package edu.kit.kastel.vads.compiler.ir.node

import java.lang.System.identityHashCode

const val HASH_PRIME = 31


enum class BinaryOperator(val isCommutative: Boolean, val hasSideEffect: Boolean) {
    ADD(true, false),
    SUBTRACT(false, false),
    MULTIPLY(true, false),
    DIVIDE(false, true),
    MODULO(false, true),
}

open class BinaryOperationNode(
    block: Block,
    val operator: BinaryOperator,
    val left: Node,
    val right: Node,
    val sideEffect: Node? = null,
) : Node(block, listOfNotNull(left, right, sideEffect).toMutableList()) {

    override fun hasSideEffect(): Boolean = sideEffect != null

    override fun info() = operator.name

    init {
        assert(operator.hasSideEffect == (sideEffect != null)) {
            "Operator $operator has side effect: ${operator.hasSideEffect}, but side effect is ${sideEffect != null}"
        }
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    override fun equals(other: Any?): Boolean {
        if (sideEffect != null) return this === other
        if (other !is BinaryOperationNode || operator != other.operator) return false
        return left === other.left && right === other.right
                || (operator.isCommutative && left === other.right && right === other.left)
    }

    override fun hashCode(): Int {
        if (sideEffect != null) return identityHashCode(this)
        val leftHash = identityHashCode(left)
        val rightHash = identityHashCode(right)
        val operandsHash = if (operator.isCommutative) {
            leftHash xor rightHash
        } else {
            leftHash * HASH_PRIME + rightHash
        }
        return operator.hashCode() * HASH_PRIME + operandsHash
    }
}
