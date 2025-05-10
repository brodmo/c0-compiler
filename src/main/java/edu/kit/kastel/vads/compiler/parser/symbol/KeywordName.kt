package edu.kit.kastel.vads.compiler.parser.symbol

import edu.kit.kastel.vads.compiler.lexer.KeywordType

@JvmRecord
internal data class KeywordName(val type: KeywordType) : Name {
    override fun asString(): String {
        return this.type.keyword()
    }
}
