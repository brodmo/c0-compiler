package edu.kit.kastel.vads.compiler.ir.node

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
            else -> left == other.left && right == other.right
        }
    }

    override fun hashCode(): Int = when {
        sideEffect != null -> System.identityHashCode(this)
        operator.isCommutative -> arrayOf<Any>(operator, System.identityHashCode(left) xor System.identityHashCode(right)).contentHashCode()
        else -> arrayOf<Any>(operator, System.identityHashCode(left), System.identityHashCode(right)).contentHashCode()
    }
}
