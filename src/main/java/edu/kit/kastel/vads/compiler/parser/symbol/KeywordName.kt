package edu.kit.kastel.vads.compiler.parser.symbol

import edu.kit.kastel.vads.compiler.lexer.KeywordType

internal data class KeywordName(val type: KeywordType) : Name {
    override fun asString(): String {
        return this.type.keyword()
    }
}
