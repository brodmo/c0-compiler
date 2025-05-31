package edu.kit.kastel.vads.compiler.parser

import edu.kit.kastel.vads.compiler.lexer.*

class TokenSource {
    private val tokens: List<Token>
    private var idx: Int = 0

    constructor(lexer: Lexer) {
        this.tokens = generateSequence { lexer.nextToken() }.toList()
    }

    fun peek(): Token = tokens.getOrNull(idx) ?: throw ParseException("reached end of file")

    fun consume(): Token = peek().also { idx++ }

    fun hasMore(): Boolean = idx < tokens.size

    fun expectKeyword(type: KeywordType): Keyword =
        expectAndConsume<Keyword>("keyword '${type.keyword}'") { it.type == type }

    fun expectSeparator(type: SeparatorType): Separator =
        expectAndConsume<Separator>("separator '${type.value}'") { it.type == type }

    fun expectOperator(type: OperatorType): Operator =
        expectAndConsume<Operator>("operator '${type.value}'") { it.type == type }

    fun expectIdentifier(): Identifier =
        expectAndConsume<Identifier>("identifier")

    fun expectNumberLiteral(): NumberLiteral =
        expectAndConsume<NumberLiteral>("number literal")

    fun expectAnyOperator(vararg types: OperatorType): Operator =
        expectAndConsume<Operator>(
            "one of operators ${types.joinToString(", ") { "'${it.value}'" }}"
        ) { it.type in types.toSet() }

    private inline fun <reified T : Token> expectAndConsume(
        expectedDescription: String,
        predicate: ((T) -> Boolean) = { true }
    ): T {
        val token = peek()
        if (token !is T) {
            throw ParseException(
                "expected $expectedDescription (type ${T::class.simpleName}) " +
                        "but got ${token::class.simpleName} ($token)"
            )
        }
        if (!predicate(token)) {
            throw ParseException(
                "expected $expectedDescription but got $token (which did not satisfy the condition)"
            )
        }
        consume()
        return token
    }
}
