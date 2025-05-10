package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Position
import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.Span.SimpleSpan
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

data class ReturnTree(val expression: ExpressionTree, val start: Position) : StatementTree {
    override val span = SimpleSpan(this.start, this.expression.span.end)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }
}
