package edu.kit.kastel.vads.compiler.ir

import edu.kit.kastel.vads.compiler.ir.node.Block
import edu.kit.kastel.vads.compiler.ir.node.DivNode
import edu.kit.kastel.vads.compiler.ir.node.ModNode
import edu.kit.kastel.vads.compiler.ir.node.Node
import edu.kit.kastel.vads.compiler.ir.optimize.Optimizer
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper
import edu.kit.kastel.vads.compiler.lexer.Operator
import edu.kit.kastel.vads.compiler.parser.ast.*
import edu.kit.kastel.vads.compiler.parser.symbol.Name
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor
import java.util.*

/** SSA translation as described in
 * [`Simple and Efficient Construction of Static Single Assignment Form`](https://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf).
 *
 * This implementation also tracks side effect edges that can be used to avoid reordering of operations that cannot be
 * reordered.
 *
 * We recommend to read the paper to better understand the mechanics implemented here. */
class SsaTranslation(private val function: FunctionTree, optimizer: Optimizer) {
    private val constructor: GraphConstructor = GraphConstructor(optimizer, function.name.name.asString())

    fun translate(): IrGraph {
        val visitor = SsaTranslationVisitor()
        this.function.accept(visitor, this)
        return this.constructor.graph
    }

    private fun writeVariable(variable: Name, block: Block, value: Node) {
        this.constructor.writeVariable(variable, block, value)
    }

    private fun readVariable(variable: Name, block: Block): Node {
        return this.constructor.readVariable(variable, block)
    }

    private fun currentBlock(): Block {
        return this.constructor.currentBlock
    }

    private class SsaTranslationVisitor : Visitor<SsaTranslation, Node?> {
        private val debugStack: Deque<DebugInfo> = ArrayDeque()

        private fun pushSpan(tree: Tree) {
            debugStack.push(DebugInfoHelper.debugInfo)
            DebugInfoHelper.debugInfo = DebugInfo.SourceInfo(tree.span)
        }

        private fun popSpan() {
            DebugInfoHelper.debugInfo = debugStack.pop()
        }

        override fun visit(assignmentTree: AssignmentTree, data: SsaTranslation): Node? {
            pushSpan(assignmentTree)

            val desugar: ((Node, Node) -> Node)? = when (assignmentTree.operator.type) {
                Operator.OperatorType.ASSIGN_MINUS -> { left, right ->
                    data.constructor.newSub(left, right)
                }

                Operator.OperatorType.ASSIGN_PLUS -> { left, right ->
                    data.constructor.newAdd(left, right)
                }

                Operator.OperatorType.ASSIGN_MUL -> { left, right ->
                    data.constructor.newMul(left, right)
                }

                Operator.OperatorType.ASSIGN_DIV -> { lhs, rhs ->
                    projResultDivMod(data, data.constructor.newDiv(lhs, rhs))
                }

                Operator.OperatorType.ASSIGN_MOD -> { lhs, rhs ->
                    projResultDivMod(data, data.constructor.newMod(lhs, rhs))
                }

                Operator.OperatorType.ASSIGN -> null
                else -> throw IllegalArgumentException("not an assignment operator ${assignmentTree.operator}")
            }

            when (val lvalue = assignmentTree.lValue) {
                is LValueIdentTree -> {
                    var rhs = assignmentTree.expression.accept(this, data)
                        ?: error("Assignment expression must produce a value")

                    if (desugar != null) {
                        rhs = desugar(data.readVariable(lvalue.name.name, data.currentBlock()), rhs)
                    }
                    data.writeVariable(lvalue.name.name, data.currentBlock(), rhs)
                }
            }
            popSpan()
            return null
        }

        override fun visit(binaryOperationTree: BinaryOperationTree, data: SsaTranslation): Node {
            pushSpan(binaryOperationTree)

            val lhs = binaryOperationTree.lhs.accept(this, data)
                ?: error("Left-hand side of binary operation must produce a value")
            val rhs = binaryOperationTree.rhs.accept(this, data)
                ?: error("Right-hand side of binary operation must produce a value")

            val res = when (binaryOperationTree.operatorType) {
                Operator.OperatorType.MINUS -> data.constructor.newSub(lhs, rhs)
                Operator.OperatorType.PLUS -> data.constructor.newAdd(lhs, rhs)
                Operator.OperatorType.MUL -> data.constructor.newMul(lhs, rhs)
                Operator.OperatorType.DIV -> projResultDivMod(data, data.constructor.newDiv(lhs, rhs))
                Operator.OperatorType.MOD -> projResultDivMod(data, data.constructor.newMod(lhs, rhs))
                else -> throw IllegalArgumentException("not a binary expression operator ${binaryOperationTree.operatorType}")
            }

            popSpan()
            return res
        }

        override fun visit(blockTree: BlockTree, data: SsaTranslation): Node? {
            pushSpan(blockTree)

            for (statement in blockTree.statements) {
                statement.accept(this, data)
                // skip everything after a return in a block
                if (statement is ReturnTree) {
                    break
                }
            }

            popSpan()
            return null
        }

        override fun visit(declarationTree: DeclarationTree, data: SsaTranslation): Node? {
            pushSpan(declarationTree)

            if (declarationTree.initializer != null) {
                val rhs = declarationTree.initializer.accept(this, data)
                    ?: error("Declaration initializer must produce a value")
                data.writeVariable(declarationTree.name.name, data.currentBlock(), rhs)
            }

            popSpan()
            return null
        }

        override fun visit(functionTree: FunctionTree, data: SsaTranslation): Node? {
            pushSpan(functionTree)

            val start = data.constructor.newStart()
            data.constructor.writeCurrentSideEffect(data.constructor.newSideEffectProj(start))
            functionTree.body.accept(this, data)

            popSpan()
            return null
        }

        override fun visit(identExpressionTree: IdentExpressionTree, data: SsaTranslation): Node {
            pushSpan(identExpressionTree)
            val value = data.readVariable(identExpressionTree.name.name, data.currentBlock())
            popSpan()
            return value
        }

        override fun visit(literalTree: LiteralTree, data: SsaTranslation): Node? {
            pushSpan(literalTree)
            val value = literalTree.parseValue() ?: error("LiteralTree must produce a value")
            val node = data.constructor.newConstInt(value.toInt())
            popSpan()
            return node
        }

        override fun visit(lValueIdentTree: LValueIdentTree, data: SsaTranslation): Node? {
            return null
        }

        override fun visit(nameTree: NameTree, data: SsaTranslation): Node? {
            return null
        }

        override fun visit(negateTree: NegateTree, data: SsaTranslation): Node {
            pushSpan(negateTree)

            val node = negateTree.expression.accept(this, data)
                ?: error("Negation expression must produce a value")
            val res = data.constructor.newSub(data.constructor.newConstInt(0), node)

            popSpan()
            return res
        }

        override fun visit(programTree: ProgramTree, data: SsaTranslation): Node? {
            throw UnsupportedOperationException()
        }

        override fun visit(returnTree: ReturnTree, data: SsaTranslation): Node? {
            pushSpan(returnTree)

            val node = returnTree.expression.accept(this, data)
                ?: error("Return expression must produce a value")
            val ret = data.constructor.newReturn(node)
            data.constructor.graph.endBlock.addPredecessor(ret)

            popSpan()
            return null
        }

        override fun visit(typeTree: TypeTree, data: SsaTranslation): Node? {
            throw UnsupportedOperationException()
        }

        private fun projResultDivMod(data: SsaTranslation, divMod: Node): Node {
            // make sure we actually have a div or a mod, as optimizations could
            // have changed it to something else already
            if (divMod !is DivNode && divMod !is ModNode) {
                return divMod
            }
            val projSideEffect = data.constructor.newSideEffectProj(divMod)
            data.constructor.writeCurrentSideEffect(projSideEffect)
            return data.constructor.newResultProj(divMod)
        }
    }
}