package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

data class NegateTree(val expression: ExpressionTree, val minusPos: Span) : ExpressionTree {
    override val span = this.minusPos.merge(this.expression.span)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }
}
