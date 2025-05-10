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
import java.util.function.BinaryOperator

/** SSA translation as described in
 * [`Simple and Efficient Construction of Static Single Assignment Form`](https://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf).
 *
 * This implementation also tracks side effect edges that can be used to avoid reordering of operations that cannot be
 * reordered.
 *
 * We recommend to read the paper to better understand the mechanics implemented here. */
class SsaTranslation(private val function: FunctionTree, optimizer: Optimizer) {
    private val constructor: GraphConstructor

    init {
        this.constructor = GraphConstructor(optimizer, function.name.name.asString())
    }

    fun translate(): IrGraph {
        val visitor = SsaTranslationVisitor()
        this.function.accept<SsaTranslation, Optional<Node>>(visitor, this)
        return this.constructor.graph()
    }

    private fun writeVariable(variable: Name, block: Block, value: Node) {
        this.constructor.writeVariable(variable, block, value)
    }

    private fun readVariable(variable: Name, block: Block): Node {
        return this.constructor.readVariable(variable, block)
    }

    private fun currentBlock(): Block {
        return this.constructor.currentBlock()
    }

    private class SsaTranslationVisitor : Visitor<SsaTranslation, Optional<Node>> {
        private val debugStack: Deque<DebugInfo> = ArrayDeque<DebugInfo>()

        fun pushSpan(tree: Tree) {
            this.debugStack.push(DebugInfoHelper.debugInfo)
            DebugInfoHelper.debugInfo = DebugInfo.SourceInfo(tree.span)
        }

        fun popSpan() {
            DebugInfoHelper.debugInfo = this.debugStack.pop()
        }

        override fun visit(assignmentTree: AssignmentTree, data: SsaTranslation): Optional<Node> {
            pushSpan(assignmentTree)
            val desugar: BinaryOperator<Node>? = when (assignmentTree.operator.type) {
                Operator.OperatorType.ASSIGN_MINUS -> BinaryOperator { left: Node, right: Node ->
                    data.constructor.newSub(left, right)
                }

                Operator.OperatorType.ASSIGN_PLUS -> BinaryOperator { left: Node, right: Node ->
                    data.constructor.newAdd(left, right)
                }

                Operator.OperatorType.ASSIGN_MUL -> BinaryOperator { left: Node, right: Node ->
                    data.constructor.newMul(left, right)
                }

                Operator.OperatorType.ASSIGN_DIV -> BinaryOperator { lhs: Node, rhs: Node ->
                    projResultDivMod(data, data.constructor.newDiv(lhs, rhs))
                }

                Operator.OperatorType.ASSIGN_MOD -> BinaryOperator { lhs: Node, rhs: Node ->
                    projResultDivMod(data, data.constructor.newMod(lhs, rhs))
                }

                Operator.OperatorType.ASSIGN -> null
                else -> throw IllegalArgumentException("not an assignment operator " + assignmentTree.operator)
            }

            when (val lvalue = assignmentTree.lValue) {
                is LValueIdentTree -> {
                    var rhs = assignmentTree.expression.accept<SsaTranslation, Optional<Node>>(this, data).orElseThrow()
                    if (desugar != null) {
                        rhs = desugar.apply(data.readVariable(lvalue.name.name, data.currentBlock()), rhs)
                    }
                    data.writeVariable(lvalue.name.name, data.currentBlock(), rhs)
                }
            }
            popSpan()
            return NOT_AN_EXPRESSION
        }

        override fun visit(binaryOperationTree: BinaryOperationTree, data: SsaTranslation): Optional<Node> {
            pushSpan(binaryOperationTree)
            val lhs = binaryOperationTree.lhs.accept<SsaTranslation, Optional<Node>>(this, data).orElseThrow()
            val rhs = binaryOperationTree.rhs.accept<SsaTranslation, Optional<Node>>(this, data).orElseThrow()
            val res = when (binaryOperationTree.operatorType) {
                Operator.OperatorType.MINUS -> data.constructor.newSub(lhs, rhs)
                Operator.OperatorType.PLUS -> data.constructor.newAdd(lhs, rhs)
                Operator.OperatorType.MUL -> data.constructor.newMul(lhs, rhs)
                Operator.OperatorType.DIV -> projResultDivMod(data, data.constructor.newDiv(lhs, rhs))
                Operator.OperatorType.MOD -> projResultDivMod(data, data.constructor.newMod(lhs, rhs))
                else -> throw IllegalArgumentException("not a binary expression operator " + binaryOperationTree.operatorType)
            }
            popSpan()
            return Optional.of<Node>(res)
        }

        override fun visit(blockTree: BlockTree, data: SsaTranslation): Optional<Node> {
            pushSpan(blockTree)
            for (statement in blockTree.statements) {
                statement.accept<SsaTranslation, Optional<Node>>(this, data)
                // skip everything after a return in a block
                if (statement is ReturnTree) {
                    break
                }
            }
            popSpan()
            return NOT_AN_EXPRESSION
        }

        override fun visit(declarationTree: DeclarationTree, data: SsaTranslation): Optional<Node> {
            pushSpan(declarationTree)
            if (declarationTree.initializer != null) {
                val rhs = declarationTree.initializer.accept<SsaTranslation, Optional<Node>>(this, data).orElseThrow()
                data.writeVariable(declarationTree.name.name, data.currentBlock(), rhs)
            }
            popSpan()
            return NOT_AN_EXPRESSION
        }

        override fun visit(functionTree: FunctionTree, data: SsaTranslation): Optional<Node> {
            pushSpan(functionTree)
            val start = data.constructor.newStart()
            data.constructor.writeCurrentSideEffect(data.constructor.newSideEffectProj(start))
            functionTree.body.accept<SsaTranslation, Optional<Node>>(this, data)
            popSpan()
            return NOT_AN_EXPRESSION
        }

        override fun visit(identExpressionTree: IdentExpressionTree, data: SsaTranslation): Optional<Node> {
            pushSpan(identExpressionTree)
            val value = data.readVariable(identExpressionTree.name.name, data.currentBlock())
            popSpan()
            return Optional.of<Node>(value)
        }

        override fun visit(literalTree: LiteralTree, data: SsaTranslation): Optional<Node> {
            pushSpan(literalTree)
            val node = data.constructor.newConstInt(literalTree.parseValue().orElseThrow().toInt())
            popSpan()
            return Optional.of<Node>(node)
        }

        override fun visit(lValueIdentTree: LValueIdentTree, data: SsaTranslation): Optional<Node> {
            return NOT_AN_EXPRESSION
        }

        override fun visit(nameTree: NameTree, data: SsaTranslation): Optional<Node> {
            return NOT_AN_EXPRESSION
        }

        override fun visit(negateTree: NegateTree, data: SsaTranslation): Optional<Node> {
            pushSpan(negateTree)
            val node = negateTree.expression.accept<SsaTranslation, Optional<Node>>(this, data).orElseThrow()
            val res = data.constructor.newSub(data.constructor.newConstInt(0), node)
            popSpan()
            return Optional.of<Node>(res)
        }

        override fun visit(programTree: ProgramTree, data: SsaTranslation): Optional<Node> {
            throw UnsupportedOperationException()
        }

        override fun visit(returnTree: ReturnTree, data: SsaTranslation): Optional<Node> {
            pushSpan(returnTree)
            val node = returnTree.expression.accept<SsaTranslation, Optional<Node>>(this, data).orElseThrow()
            val ret = data.constructor.newReturn(node)
            data.constructor.graph().endBlock().addPredecessor(ret)
            popSpan()
            return NOT_AN_EXPRESSION
        }

        override fun visit(typeTree: TypeTree, data: SsaTranslation): Optional<Node> {
            throw UnsupportedOperationException()
        }

        fun projResultDivMod(data: SsaTranslation, divMod: Node): Node {
            // make sure we actually have a div or a mod, as optimizations could
            // have changed it to something else already
            if (!(divMod is DivNode || divMod is ModNode)) {
                return divMod
            }
            val projSideEffect = data.constructor.newSideEffectProj(divMod)
            data.constructor.writeCurrentSideEffect(projSideEffect)
            return data.constructor.newResultProj(divMod)
        }

        companion object {
            private val NOT_AN_EXPRESSION = Optional.empty<Node>()
        }
    }
}
