package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.type.Type
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

@JvmRecord
data class TypeTree(val type: Type, override val span: Span) : Tree {
    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }
}
