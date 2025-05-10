package edu.kit.kastel.vads.compiler.lexer

import edu.kit.kastel.vads.compiler.Span

@JvmRecord
data class ErrorToken(val value: String, override val span: Span) : Token {
    override fun asString(): String {
        return this.value
    }
}
