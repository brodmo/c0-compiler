package edu.kit.kastel.vads.compiler.lexer

import edu.kit.kastel.vads.compiler.Span

sealed interface Token {
    val span: Span
    val asString: String

    fun isKeyword(keywordType: KeywordType): Boolean = false
    fun isOperator(operatorType: OperatorType): Boolean = false
    fun isSeparator(separatorType: SeparatorType): Boolean = false

}

data class Identifier(val value: String, override val span: Span) : Token {
    override val asString: String = this.value
}

data class NumberLiteral(val value: String, val base: Int, override val span: Span) : Token {
    override val asString: String = this.value
}

data class ErrorToken(val value: String, override val span: Span) : Token {
    override val asString: String = this.value
}

data class Keyword(val type: KeywordType, override val span: Span) : Token {
    override fun isKeyword(keywordType: KeywordType): Boolean = this.type == keywordType
    override val asString: String = this.type.keyword
}

data class Operator(val type: OperatorType, override val span: Span) : Token {
    override fun isOperator(operatorType: OperatorType): Boolean = this.type == operatorType
    override val asString: String = this.type.toString()
}

data class Separator(val type: SeparatorType, override val span: Span) : Token {
    override fun isSeparator(separatorType: SeparatorType): Boolean = this.type == separatorType
    override val asString: String = this.type.toString()
}
