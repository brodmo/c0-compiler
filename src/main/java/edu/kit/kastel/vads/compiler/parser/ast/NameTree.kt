package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.symbol.Name
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

data class NameTree(val name: Name, override val span: Span) : Tree {
    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }
}
