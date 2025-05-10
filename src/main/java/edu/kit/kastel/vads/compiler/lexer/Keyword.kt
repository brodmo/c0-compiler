package edu.kit.kastel.vads.compiler.lexer

import edu.kit.kastel.vads.compiler.Span

@JvmRecord
data class Keyword(val type: KeywordType, override val span: Span) : Token {
    override fun isKeyword(keywordType: KeywordType): Boolean {
        return this.type == keywordType
    }

    override fun asString(): String {
        return this.type.keyword()
    }
}
