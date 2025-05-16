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

    init {
        assert(operator.hasSideEffect == (sideEffect != null)) {
            "Operator $operator has side effect: ${operator.hasSideEffect}, but side effect is ${sideEffect != null}"
        }
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    override fun equals(other: Any?): Boolean {
        return when {
            other !is BinaryOperationNode || operator != other.operator -> false
            sideEffect != null -> this === other // something about value numbering and being conservative? idk
            operator.isCommutative -> setOf(left, right) == setOf(other.left, other.right)
            // this is recursive and could lead to runtime issues
            else -> left == other.left && right == other.right
        }
    }

    override fun hashCode(): Int {
        if (sideEffect != null) {
            return identityHashCode(this)
        }
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
