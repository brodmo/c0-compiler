package edu.kit.kastel.vads.compiler.ir

import edu.kit.kastel.vads.compiler.ir.node.BinaryOperator
import edu.kit.kastel.vads.compiler.ir.node.Block
import edu.kit.kastel.vads.compiler.ir.node.Node
import edu.kit.kastel.vads.compiler.ir.optimize.Optimizer
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper
import edu.kit.kastel.vads.compiler.lexer.Operator
import edu.kit.kastel.vads.compiler.parser.ast.*
import edu.kit.kastel.vads.compiler.parser.symbol.Name
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor
import java.util.*

private val operatorMap = mapOf(
    Operator.OperatorType.MINUS to BinaryOperator.SUBTRACT,
    Operator.OperatorType.ASSIGN_MINUS to BinaryOperator.SUBTRACT,
    Operator.OperatorType.PLUS to BinaryOperator.ADD,
    Operator.OperatorType.ASSIGN_PLUS to BinaryOperator.ADD,
    Operator.OperatorType.MUL to BinaryOperator.MULTIPLY,
    Operator.OperatorType.ASSIGN_MUL to BinaryOperator.MULTIPLY,
    Operator.OperatorType.DIV to BinaryOperator.DIVIDE,
    Operator.OperatorType.ASSIGN_DIV to BinaryOperator.DIVIDE,
    Operator.OperatorType.MOD to BinaryOperator.MODULO,
    Operator.OperatorType.ASSIGN_MOD to BinaryOperator.MODULO
)

/**
 * Translates a function AST to SSA form.
 *
 * SSA translation as described in
 * [`Simple and Efficient Construction of Static Single Assignment Form`](https://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf).
 *
 * This implementation also tracks side effect edges that can be used to avoid reordering of operations that cannot be
 * reordered.
 */
fun translateToSsa(function: FunctionTree, optimizer: Optimizer): IrGraph {
    val constructor = GraphConstructor(optimizer, function.name.name.asString())
    val context = SsaContext(constructor)
    val visitor = SsaTranslationVisitor()
    function.accept(visitor, context)
    return constructor.graph
}

/**
 * Context object that holds all the state and operations needed for SSA translation.
 * This encapsulates the GraphConstructor and provides a clean interface for the visitor.
 */
internal class SsaContext(private val graphConstructor: GraphConstructor) {
    val currentBlock: Block get() = graphConstructor.currentBlock
    fun writeVariable(name: Name, value: Node) = graphConstructor.writeVariable(name, currentBlock, value)
    fun readVariable(name: Name): Node = graphConstructor.readVariable(name, currentBlock)

    fun createConstInt(value: Int): Node = graphConstructor.newConstInt(value)

    fun createBinaryOperation(operatorType: Operator.OperatorType, left: Node, right: Node): Node {
        val binaryOperator = operatorMap[operatorType]
            ?: throw IllegalArgumentException("Unsupported binary operator: $operatorType")

        val result = graphConstructor.newBinaryOperation(binaryOperator, left, right)

        return if (result.hasSideEffect()) {
            val projSideEffect = graphConstructor.newSideEffectProj(result)
            graphConstructor.writeCurrentSideEffect(projSideEffect)
            graphConstructor.newResultProj(result)
        } else {
            result
        }
    }

    fun createReturn(value: Node): Node {
        val returnNode = graphConstructor.newReturn(value)
        graphConstructor.graph.endBlock.addPredecessor(returnNode)
        return returnNode
    }

    fun initializeFunction(): Node {
        val start = graphConstructor.newStart()
        graphConstructor.writeCurrentSideEffect(graphConstructor.newSideEffectProj(start))
        return start
    }
}

/**
 * Visitor that performs the actual AST-to-SSA translation.
 * Uses SsaContext to access all necessary operations.
 */
internal class SsaTranslationVisitor : Visitor<SsaContext, Node?> {
    private val debugStack: Deque<DebugInfo> = ArrayDeque()

    private inline fun <T> withSpan(tree: Tree, context: SsaContext, block: () -> T): T {
        debugStack.push(DebugInfoHelper.debugInfo)
        DebugInfoHelper.debugInfo = DebugInfo.SourceInfo(tree.span)
        try {
            return block()
        } finally {
            DebugInfoHelper.debugInfo = debugStack.pop()
        }
    }

    override fun visit(assignmentTree: AssignmentTree, context: SsaContext): Node? = withSpan(assignmentTree, context) {
        when (val lvalue = assignmentTree.lValue) {
            is LValueIdentTree -> {
                var rhs = assignmentTree.expression.accept(this, context)
                    ?: error("Assignment expression must produce a value")

                // Handle compound assignments (+=, -=, *=, /=, %=)
                if (assignmentTree.operator.type != Operator.OperatorType.ASSIGN) {
                    val lhs = context.readVariable(lvalue.name.name)
                    rhs = context.createBinaryOperation(assignmentTree.operator.type, lhs, rhs)
                }

                context.writeVariable(lvalue.name.name, rhs)
            }
        }
        null
    }

    override fun visit(binaryOperationTree: BinaryOperationTree, context: SsaContext): Node = withSpan(binaryOperationTree, context) {
        val lhs = binaryOperationTree.lhs.accept(this, context)
            ?: error("Left-hand side of binary operation must produce a value")
        val rhs = binaryOperationTree.rhs.accept(this, context)
            ?: error("Right-hand side of binary operation must produce a value")

        context.createBinaryOperation(binaryOperationTree.operatorType, lhs, rhs)
    }

    override fun visit(blockTree: BlockTree, context: SsaContext): Node? = withSpan(blockTree, context) {
        for (statement in blockTree.statements) {
            statement.accept(this, context)
            // Skip everything after a return in a block
            if (statement is ReturnTree) break
        }
        null
    }

    override fun visit(declarationTree: DeclarationTree, context: SsaContext): Node? = withSpan(declarationTree, context) {
        declarationTree.initializer?.let { initializer ->
            val value = initializer.accept(this, context)
                ?: error("Declaration initializer must produce a value")
            context.writeVariable(declarationTree.name.name, value)
        }
        null
    }

    override fun visit(functionTree: FunctionTree, context: SsaContext): Node? = withSpan(functionTree, context) {
        context.initializeFunction()
        functionTree.body.accept(this, context)
        null
    }

    override fun visit(identExpressionTree: IdentExpressionTree, context: SsaContext): Node = withSpan(identExpressionTree, context) {
        context.readVariable(identExpressionTree.name.name)
    }

    override fun visit(literalTree: LiteralTree, context: SsaContext): Node = withSpan(literalTree, context) {
        val value = literalTree.parseValue() ?: error("LiteralTree must produce a value")
        context.createConstInt(value.toInt())
    }

    override fun visit(lValueIdentTree: LValueIdentTree, context: SsaContext): Node? = null

    override fun visit(nameTree: NameTree, context: SsaContext): Node? = null

    override fun visit(negateTree: NegateTree, context: SsaContext): Node = withSpan(negateTree, context) {
        val operand = negateTree.expression.accept(this, context)
            ?: error("Negation expression must produce a value")
        val zero = context.createConstInt(0)
        context.createBinaryOperation(Operator.OperatorType.MINUS, zero, operand)
    }

    override fun visit(programTree: ProgramTree, context: SsaContext): Node? {
        throw UnsupportedOperationException()
    }

    override fun visit(returnTree: ReturnTree, context: SsaContext): Node? = withSpan(returnTree, context) {
        val value = returnTree.expression.accept(this, context)
            ?: error("Return expression must produce a value")
        context.createReturn(value)
        null
    }

    override fun visit(typeTree: TypeTree, context: SsaContext): Node? {
        throw UnsupportedOperationException()
    }
}
