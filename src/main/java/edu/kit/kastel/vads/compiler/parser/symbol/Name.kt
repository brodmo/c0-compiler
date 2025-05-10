package edu.kit.kastel.vads.compiler.parser.symbol

import edu.kit.kastel.vads.compiler.lexer.Identifier
import edu.kit.kastel.vads.compiler.lexer.Keyword

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
