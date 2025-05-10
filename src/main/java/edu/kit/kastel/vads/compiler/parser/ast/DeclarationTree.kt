package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

data class DeclarationTree(val type: TypeTree, val name: NameTree, val initializer: ExpressionTree?) : StatementTree {
    override val span: Span = this.initializer?.let { type.span.merge(it.span) } ?: type.span.merge(name.span)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }
}
