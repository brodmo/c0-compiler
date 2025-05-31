package edu.kit.kastel.vads.compiler.parser

import edu.kit.kastel.vads.compiler.lexer.*

class TokenSource {
    private val tokens: List<Token>
    private var idx: Int = 0

    constructor(lexer: Lexer) {
        this.tokens = generateSequence { lexer.nextToken() }.toList()
    }

    internal constructor(tokens: List<Token>) {
        this.tokens = tokens.toList()
    }

    fun peek(): Token {
        expectHasMore()
        return tokens[idx]
    }

    fun expectKeyword(type: KeywordType): Keyword {
        val token = peek()
        if (token !is Keyword || token.type != type) {
            throw ParseException("expected keyword '$type' but got $token")
        }
        idx++
        return token
    }

    fun expectSeparator(type: SeparatorType): Separator {
        val token = peek()
        if (token !is Separator || token.type != type) {
            throw ParseException("expected separator '$type' but got $token")
        }
        idx++
        return token
    }

    fun expectOperator(type: OperatorType): Operator {
        val token = peek()
        if (token !is Operator || token.type != type) {
            throw ParseException("expected operator '$type' but got $token")
        }
        idx++
        return token
    }

    fun expectIdentifier(): Identifier {
        val token = peek()
        if (token !is Identifier) {
            throw ParseException("expected identifier but got $token")
        }
        idx++
        return token
    }

    fun consume(): Token {
        val token = peek()
        idx++
        return token
    }

    fun hasMore(): Boolean {
        return idx < tokens.size
    }

    private fun expectHasMore() {
        if (idx >= tokens.size) {
            throw ParseException("reached end of file")
        }
    }
}