package edu.kit.kastel.vads.compiler.parser.visitor

import edu.kit.kastel.vads.compiler.parser.ast.*

interface Visitor<T, R> {
    fun visit(assignmentTree: AssignmentTree, data: T): R

    fun visit(binaryOperationTree: BinaryOperationTree, data: T): R

    fun visit(blockTree: BlockTree, data: T): R

    fun visit(declarationTree: DeclarationTree, data: T): R

    fun visit(functionTree: FunctionTree, data: T): R

    fun visit(identExpressionTree: IdentExpressionTree, data: T): R

    fun visit(literalTree: LiteralTree, data: T): R

    fun visit(lValueIdentTree: LValueIdentTree, data: T): R

    fun visit(nameTree: NameTree, data: T): R

    fun visit(negateTree: NegateTree, data: T): R

    fun visit(programTree: ProgramTree, data: T): R

    fun visit(returnTree: ReturnTree, data: T): R

    fun visit(typeTree: TypeTree, data: T): R
}
