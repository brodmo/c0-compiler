package edu.kit.kastel.vads.compiler.lexer

enum class KeywordType(val keyword: String) {
    STRUCT("struct"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    FOR("for"),
    CONTINUE("continue"),
    BREAK("break"),
    RETURN("return"),
    ASSERT("assert"),
    TRUE("true"),
    FALSE("false"),
    NULL("NULL"),
    PRINT("print"),
    READ("read"),
    ALLOC("alloc"),
    ALLOC_ARRAY("alloc_array"),
    INT("int"),
    BOOL("bool"),
    VOID("void"),
    CHAR("char"),
    STRING("string"),
    ;


    override fun toString(): String = keyword
}

enum class OperatorType(private val value: String) {
    MINUS("-"), ASSIGN_MINUS("-="),
    PLUS("+"), ASSIGN_PLUS("+="),
    MUL("*"), ASSIGN_MUL("*="),
    DIV("/"), ASSIGN_DIV("/="),
    MOD("%"), ASSIGN_MOD("%="),
    ASSIGN("="),
    ;

    override fun toString(): String = this.value
}

enum class SeparatorType(private val value: String) {
    PAREN_OPEN("("),
    PAREN_CLOSE(")"),
    BRACE_OPEN("{"),
    BRACE_CLOSE("}"),
    SEMICOLON(";");

    override fun toString(): String = this.value
}
