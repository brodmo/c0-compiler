package edu.kit.kastel.vads.compiler.lexer

enum class KeywordType(private val keyword: String) {
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

    fun keyword(): String {
        return keyword
    }

    override fun toString(): String {
        return keyword()
    }
}
