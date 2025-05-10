package edu.kit.kastel.vads.compiler.lexer

import edu.kit.kastel.vads.compiler.Span

data class Operator(val type: OperatorType, override val span: Span) : Token {
    override fun isOperator(operatorType: OperatorType): Boolean {
        return this.type == operatorType
    }

    override fun asString(): String {
        return this.type.toString()
    }

    enum class OperatorType(private val value: String) {
        ASSIGN_MINUS("-="),
        MINUS("-"),
        ASSIGN_PLUS("+="),
        PLUS("+"),
        MUL("*"),
        ASSIGN_MUL("*="),
        ASSIGN_DIV("/="),
        DIV("/"),
        ASSIGN_MOD("%="),
        MOD("%"),
        ASSIGN("="),
        ;

        override fun toString(): String {
            return this.value
        }
    }
}
