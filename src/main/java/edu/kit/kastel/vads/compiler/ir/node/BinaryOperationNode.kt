package edu.kit.kastel.vads.compiler.ir.node

sealed class BinaryOperationNode : Node {
    protected constructor(block: Block, left: Node, right: Node) : super(block, left, right)

    protected constructor(block: Block, left: Node, right: Node, sideEffect: Node) : super(
        block,
        left,
        right,
        sideEffect
    )

    override fun equals(other: Any?): Boolean {
        if (other !is BinaryOperationNode) {
            return false
        }
        return other.javaClass == this.javaClass && this.predecessor(LEFT) === other.predecessor(LEFT) && this.predecessor(
            RIGHT
        ) === other.predecessor(RIGHT)
    }

    override fun hashCode(): Int {
        return (predecessorHash(this, LEFT) * 31 + predecessorHash(
            this,
            RIGHT
        )) xor this.javaClass.hashCode()
    }

    companion object {
        const val LEFT: Int = 0
        const val RIGHT: Int = 1

        internal fun commutativeHashCode(node: BinaryOperationNode): Int {
            val operands = setOf(
                predecessorHash(node, LEFT),
                predecessorHash(node, RIGHT)
            )
            return listOf(node.block(), operands).hashCode()
        }

        internal fun commutativeEquals(a: BinaryOperationNode, bObj: Any?): Boolean {
            val b = bObj as? BinaryOperationNode ?: return false
            if (a::class != b::class) return false
            val aOperands = setOf(a.predecessor(LEFT), a.predecessor(RIGHT))
            val bOperands = setOf(b.predecessor(LEFT), b.predecessor(RIGHT))
            return aOperands == bOperands
        }
    }
}
