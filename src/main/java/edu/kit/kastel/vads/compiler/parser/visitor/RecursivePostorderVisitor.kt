package edu.kit.kastel.vads.compiler.parser.visitor

import edu.kit.kastel.vads.compiler.parser.ast.*

/**
 * A visitor that traverses a tree in postorder
 * @param T a type for additional context
 * @param R a type for a return type
 */
open class RecursivePostorderVisitor<T, R>(private val visitor: Visitor<T, R>) : Visitor<T, R> {

    override fun visit(assignmentTree: AssignmentTree, context: T): R =
        visitChildren(assignmentTree, context, assignmentTree.lValue, assignmentTree.expression)

    override fun visit(binaryOperationTree: BinaryOperationTree, context: T): R =
        visitChildren(binaryOperationTree, context, binaryOperationTree.lhs, binaryOperationTree.rhs)

    override fun visit(blockTree: BlockTree, context: T): R =
        visitChildren(blockTree, context, blockTree.statements)

    override fun visit(declarationTree: DeclarationTree, context: T): R =
        visitChildren(
            declarationTree, context, listOfNotNull(
                declarationTree.type,
                declarationTree.name,
                declarationTree.initializer  // nullable
            )
        )

    override fun visit(functionTree: FunctionTree, context: T): R =
        visitChildren(functionTree, context, functionTree.returnType, functionTree.name, functionTree.body)

    override fun visit(identExpressionTree: IdentExpressionTree, context: T): R =
        visitChildren(identExpressionTree, context, identExpressionTree.name)

    override fun visit(literalTree: LiteralTree, context: T): R =
        visitChildren(literalTree, context)

    override fun visit(lValueIdentTree: LValueIdentTree, context: T): R =
        visitChildren(lValueIdentTree, context, lValueIdentTree.name)

    override fun visit(nameTree: NameTree, context: T): R =
        visitChildren(nameTree, context)

    override fun visit(negateTree: NegateTree, context: T): R =
        visitChildren(negateTree, context, negateTree.expression)

    override fun visit(programTree: ProgramTree, context: T): R =
        visitChildren(programTree, context, programTree.topLevelTrees)

    override fun visit(returnTree: ReturnTree, context: T): R =
        visitChildren(returnTree, context, returnTree.expression)

    override fun visit(typeTree: TypeTree, context: T): R =
        visitChildren(typeTree, context)

    private fun <N : Tree> visitChildren(
        node: N,
        context: T,
        vararg children: Tree
    ): R = visitChildren(node, context, children.toList())

    private fun <N : Tree> visitChildren(
        node: N,
        context: T,
        children: List<Tree>
    ): R {
        val finalContext = children.fold(context) { acc, child ->
            val result = child.accept(this, acc)
            accumulate(acc, result)
        }
        return node.accept(visitor, finalContext)
    }

    protected open fun accumulate(context: T, value: R): T = context
}
