package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.lexer.Operator
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

/**
 * Represents a binary operation (e.g., addition, subtraction) in the AST.
 */
data class BinaryOperationTree(
    val lhs: ExpressionTree,
    val rhs: ExpressionTree,
    val operatorType: Operator.OperatorType
) : ExpressionTree {
    override val span = this.lhs.span.merge(this.rhs.span)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}

/**
 * Represents an assignment statement (e.g., `x = 5`, `y += 1`) in the AST.
 */
data class AssignmentTree(
    val lValue: LValueTree,
    val operator: Operator,
    val expression: ExpressionTree
) : StatementTree {
    override val span: Span = this.lValue.span.merge(this.expression.span)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}

/**
 * Represents a negation operation (e.g., `-x`) in the AST.
 */
data class NegateTree(val expression: ExpressionTree, val minusPos: Span) : ExpressionTree {
    override val span = this.minusPos.merge(this.expression.span)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}
