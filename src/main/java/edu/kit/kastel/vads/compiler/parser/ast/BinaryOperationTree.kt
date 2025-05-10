package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.lexer.Operator
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

data class BinaryOperationTree(
    val lhs: ExpressionTree, val rhs: ExpressionTree, val operatorType: Operator.OperatorType
) : ExpressionTree {
    override val span = this.lhs.span.merge(this.rhs.span)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }
}
