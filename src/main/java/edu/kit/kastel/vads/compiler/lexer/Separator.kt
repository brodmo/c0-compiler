package edu.kit.kastel.vads.compiler.lexer

import edu.kit.kastel.vads.compiler.Span

@JvmRecord
data class Separator(val type: SeparatorType, override val span: Span) : Token {
    override fun isSeparator(separatorType: SeparatorType): Boolean {
        return this.type == separatorType
    }

    override fun asString(): String {
        return this.type.toString()
    }

    enum class SeparatorType(private val value: String) {
        PAREN_OPEN("("),
        PAREN_CLOSE(")"),
        BRACE_OPEN("{"),
        BRACE_CLOSE("}"),
        SEMICOLON(";");

        override fun toString(): String {
            return this.value
        }
    }
}
