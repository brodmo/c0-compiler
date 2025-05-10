package edu.kit.kastel.vads.compiler.parser.visitor

import edu.kit.kastel.vads.compiler.parser.ast.*

/** A visitor that does nothing and returns [Unit#INSTANCE] by default.
 * This can be used to implement operations only for specific tree types. */
interface NoOpVisitor<T> : Visitor<T, Unit> {
    override fun visit(assignmentTree: AssignmentTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(binaryOperationTree: BinaryOperationTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(blockTree: BlockTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(declarationTree: DeclarationTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(functionTree: FunctionTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(identExpressionTree: IdentExpressionTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(literalTree: LiteralTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(lValueIdentTree: LValueIdentTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(nameTree: NameTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(negateTree: NegateTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(programTree: ProgramTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(returnTree: ReturnTree, data: T): Unit {
        return Unit.INSTANCE
    }

    override fun visit(typeTree: TypeTree, data: T): Unit {
        return Unit.INSTANCE
    }
}
