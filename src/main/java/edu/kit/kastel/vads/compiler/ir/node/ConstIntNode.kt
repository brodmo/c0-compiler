package edu.kit.kastel.vads.compiler.ir.node

class ConstIntNode(block: Block, private val value: Int) : Node(block) {
    fun value(): Int {
        return this.value
    }

    override fun equals(other: Any?): Boolean {
        if (other is ConstIntNode) {
            return this.block() == other.block() && other.value == this.value
        }
        return false
    }

    override fun hashCode(): Int {
        return this.value
    }

    override fun info(): String {
        return "[" + this.value + "]"
    }
}
