package edu.kit.kastel.vads.compiler.ir.node


class ConstIntNode(block: Block, val value: Int) : Node(block, emptyList()) {
    override fun info(): String = "[$value]"

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    override fun equals(other: Any?): Boolean =
        other is ConstIntNode && safeBlock == other.safeBlock && value == other.value

    override fun hashCode(): Int = value
}
