package edu.kit.kastel.vads.compiler.ir.node

class AddNode(block: Block, left: Node, right: Node) : BinaryOperationNode(block, left, right) {
    override fun equals(other: Any?): Boolean {
        return BinaryOperationNode.Companion.commutativeEquals(this, other)
    }

    override fun hashCode(): Int {
        return BinaryOperationNode.Companion.commutativeHashCode(this)
    }
}
