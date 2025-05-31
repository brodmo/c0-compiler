package edu.kit.kastel.vads.compiler.parser.visitor

import edu.kit.kastel.vads.compiler.parser.ast.*

/** A visitor that does nothing and returns [Unit#INSTANCE] by default.
 * This can be used to implement operations only for specific tree types. */
interface NoOpVisitor<T> : Visitor<T, Unit> {
    override fun visit(assignmentTree: AssignmentTree, context: T): Unit = Unit.INSTANCE
    override fun visit(binaryOperationTree: BinaryOperationTree, context: T): Unit = Unit.INSTANCE
    override fun visit(blockTree: BlockTree, context: T): Unit = Unit.INSTANCE
    override fun visit(declarationTree: DeclarationTree, context: T): Unit = Unit.INSTANCE
    override fun visit(functionTree: FunctionTree, context: T): Unit = Unit.INSTANCE
    override fun visit(identExpressionTree: IdentExpressionTree, context: T): Unit = Unit.INSTANCE
    override fun visit(literalTree: LiteralTree, context: T): Unit = Unit.INSTANCE
    override fun visit(lValueIdentTree: LValueIdentTree, context: T): Unit = Unit.INSTANCE
    override fun visit(nameTree: NameTree, context: T): Unit = Unit.INSTANCE
    override fun visit(negateTree: NegateTree, context: T): Unit = Unit.INSTANCE
    override fun visit(programTree: ProgramTree, context: T): Unit = Unit.INSTANCE
    override fun visit(returnTree: ReturnTree, context: T): Unit = Unit.INSTANCE
    override fun visit(typeTree: TypeTree, context: T): Unit = Unit.INSTANCE
}
