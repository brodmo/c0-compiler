package edu.kit.kastel.vads.compiler.ir.node

interface NodeVisitor<T> {
    fun visit(node: BinaryOperationNode): T

    fun visit(node: ConstIntNode): T

    fun visit(node: StartNode): T
    fun visit(node: ReturnNode): T
    fun visit(node: ProjNode): T

    fun visit(node: Block): T
    fun visit(node: Phi): T
}

