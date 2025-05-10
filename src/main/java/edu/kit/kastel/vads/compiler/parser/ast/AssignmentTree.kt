package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.lexer.Operator
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

data class AssignmentTree(val lValue: LValueTree, val operator: Operator, val expression: ExpressionTree) :
    StatementTree {
    override val span: Span = this.lValue.span.merge(this.expression.span)


    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }
}
