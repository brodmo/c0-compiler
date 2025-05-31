package edu.kit.kastel.vads.compiler.parser.symbol

import edu.kit.kastel.vads.compiler.lexer.Identifier
import edu.kit.kastel.vads.compiler.lexer.Keyword
import edu.kit.kastel.vads.compiler.lexer.KeywordType

interface Name {
    fun asString(): String

    companion object {
        fun forKeyword(keyword: Keyword): Name {
            return KeywordName(keyword.type)
        }

        fun forIdentifier(identifier: Identifier): Name {
            return IdentName(identifier.value)
        }
    }
}

internal data class IdentName(val identifier: String) : Name {
    override fun asString(): String = this.identifier
}

internal data class KeywordName(val type: KeywordType) : Name {
    override fun asString(): String = this.type.keyword
}
