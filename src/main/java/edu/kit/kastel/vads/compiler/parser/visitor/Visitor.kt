package edu.kit.kastel.vads.compiler.parser.visitor

import edu.kit.kastel.vads.compiler.parser.ast.*

interface Visitor<T, R> {
    fun visit(assignmentTree: AssignmentTree, context: T): R

    fun visit(binaryOperationTree: BinaryOperationTree, context: T): R

    fun visit(blockTree: BlockTree, context: T): R

    fun visit(declarationTree: DeclarationTree, context: T): R

    fun visit(functionTree: FunctionTree, context: T): R

    fun visit(identExpressionTree: IdentExpressionTree, context: T): R

    fun visit(literalTree: LiteralTree, context: T): R

    fun visit(lValueIdentTree: LValueIdentTree, context: T): R

    fun visit(nameTree: NameTree, context: T): R

    fun visit(negateTree: NegateTree, context: T): R

    fun visit(programTree: ProgramTree, context: T): R

    fun visit(returnTree: ReturnTree, context: T): R

    fun visit(typeTree: TypeTree, context: T): R
}
