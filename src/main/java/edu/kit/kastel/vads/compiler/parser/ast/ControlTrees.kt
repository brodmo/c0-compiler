package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Position
import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.Span.SimpleSpan
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

/**
 * Represents a statement in the AST.
 * All statement nodes implement this interface.
 */
sealed interface StatementTree : Tree

/**
 * Represents a block of statements in the AST.
 */
class BlockTree(statements: List<StatementTree>, override val span: Span) : StatementTree {
    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)

    /**
     * The list of statements contained within this block.
     * This list is immutable after construction.
     */
    val statements: List<StatementTree> = statements.toList()
}

/**
 * Represents a return statement in the AST.
 */
data class ReturnTree(val expression: ExpressionTree, val start: Position) : StatementTree {
    override val span = SimpleSpan(this.start, this.expression.span.end)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}

/**
 * Represents a variable declaration statement in the AST.
 */
data class DeclarationTree(
    val type: TypeTree,
    val name: NameTree,
    val initializer: ExpressionTree?
) : StatementTree {
    override val span: Span = this.initializer?.let { type.span.merge(it.span) } ?: type.span.merge(name.span)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}
