package edu.kit.kastel.vads.compiler.lexer

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.lexer.Separator.SeparatorType

interface Token {
    val span: Span

    fun isKeyword(keywordType: KeywordType): Boolean {
        return false
    }

    fun isOperator(operatorType: Operator.OperatorType): Boolean {
        return false
    }

    fun isSeparator(separatorType: SeparatorType): Boolean {
        return false
    }

    fun asString(): String
}
