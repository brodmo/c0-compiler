package edu.kit.kastel.vads.compiler.ir.node

class DivNode(block: Block, left: Node, right: Node, sideEffect: Node) :
    BinaryOperationNode(block, left, right, sideEffect) {
    override fun equals(other: Any?): Boolean {
        // side effect, must be very careful with value numbering.
        // this is the most conservative approach
        return other === this
    }

    companion object {
        const val SIDE_EFFECT: Int = 2
    }
}
