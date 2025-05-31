package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.symbol.Name
import edu.kit.kastel.vads.compiler.parser.type.Type
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

/**
 * Represents an expression in the AST.
 * All expression nodes implement this interface.
 */
sealed interface ExpressionTree : Tree

/**
 * Represents an L-value (something that can be assigned to) in the AST.
 * All L-value nodes implement this interface.
 */
sealed interface LValueTree : Tree

/**
 * Represents a literal value (e.g., integer constant) in the AST.
 */
data class LiteralTree(
    val value: String,
    val base: Int,
    override val span: Span
) : ExpressionTree {

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)

    /**
     * Parses the string value of the literal into a Long.
     * Returns null if parsing fails or the value is out of the expected range.
     */
    fun parseValue(): Long? = try {
        val maxDecimal = Int.MIN_VALUE.toUInt().toLong()
        when (base) {
            16 -> value.substring(2).toUInt(base).toLong()
            10 -> value.toLong(base).takeIf { it in 0..maxDecimal }
            else -> throw IllegalArgumentException("unexpected base $base")
        }
    } catch (e: NumberFormatException) { null }
}

/**
 * Represents a name or identifier in the AST.
 */
data class NameTree(val name: Name, override val span: Span) : Tree {
    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}

/**
 * Represents an expression that is simply an identifier (e.g., a variable reference).
 */
data class IdentExpressionTree(val name: NameTree) : ExpressionTree {
    override val span = this.name.span

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}

/**
 * Represents an L-value that is an identifier (e.g., a variable on the left-hand side of an assignment).
 */
data class LValueIdentTree(val name: NameTree) : LValueTree {
    override val span = this.name.span

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}

/**
 * Represents a type declaration in the AST.
 */
data class TypeTree(val type: Type, override val span: Span) : Tree {
    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}
