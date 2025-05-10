package edu.kit.kastel.vads.compiler.lexer

import edu.kit.kastel.vads.compiler.Span

data class NumberLiteral(val value: String, val base: Int, override val span: Span) : Token {
    override fun asString(): String {
        return this.value
    }
}
