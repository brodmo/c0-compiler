package edu.kit.kastel.vads.compiler.lexer

import edu.kit.kastel.vads.compiler.Position
import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.lexer.Operator.OperatorType
import edu.kit.kastel.vads.compiler.lexer.Separator.SeparatorType

class Lexer private constructor(private val source: String) {
    private var pos: Int = 0
    private var lineStart: Int = 0
    private var line: Int = 0

    companion object {
        fun forString(source: String): Lexer {
            return Lexer(source)
        }
    }

    fun nextToken(): Token? {
        val error = skipWhitespace()
        if (error != null) {
            return error
        }
        if (pos >= source.length) {
            return null
        }

        val t = when (peek()) {
            '(' -> separator(SeparatorType.PAREN_OPEN)
            ')' -> separator(SeparatorType.PAREN_CLOSE)
            '{' -> separator(SeparatorType.BRACE_OPEN)
            '}' -> separator(SeparatorType.BRACE_CLOSE)
            ';' -> separator(SeparatorType.SEMICOLON)
            '-' -> singleOrAssign(OperatorType.MINUS, OperatorType.ASSIGN_MINUS)
            '+' -> singleOrAssign(OperatorType.PLUS, OperatorType.ASSIGN_PLUS)
            '*' -> singleOrAssign(OperatorType.MUL, OperatorType.ASSIGN_MUL)
            '/' -> singleOrAssign(OperatorType.DIV, OperatorType.ASSIGN_DIV)
            '%' -> singleOrAssign(OperatorType.MOD, OperatorType.ASSIGN_MOD)
            '=' -> Operator(OperatorType.ASSIGN, buildSpan(1))
            else -> {
                if (isIdentifierChar(peek())) {
                    if (isNumeric(peek())) {
                        lexNumber()
                    } else {
                        lexIdentifierOrKeyword()
                    }
                } else {
                    ErrorToken(peek().toString(), buildSpan(1))
                }
            }
        }

        return t
    }

    enum class CommentType {
        SINGLE_LINE,
        MULTI_LINE
    }

    private fun skipWhitespace(): ErrorToken? {

        var currentCommentType: CommentType? = null
        var multiLineCommentDepth = 0
        var commentStart = -1

        while (hasMore(0)) {
            when (peek()) {
                ' ', '\t' -> pos++
                '\n', '\r' -> {
                    pos++
                    lineStart = pos
                    line++
                    if (currentCommentType == CommentType.SINGLE_LINE) {
                        currentCommentType = null
                    }
                }
                '/' -> {
                    if (currentCommentType == CommentType.SINGLE_LINE) {
                        pos++
                        continue
                    }
                    if (hasMore(1)) {
                        if (peek(1) == '/' && currentCommentType == null) {
                            currentCommentType = CommentType.SINGLE_LINE
                        } else if (peek(1) == '*') {
                            currentCommentType = CommentType.MULTI_LINE
                            multiLineCommentDepth++
                        } else if (currentCommentType == CommentType.MULTI_LINE) {
                            pos++
                            continue
                        } else {
                            return null
                        }
                        commentStart = pos
                        pos += 2
                        continue
                    }
                    // are we in a multi line comment of any depth?
                    if (multiLineCommentDepth > 0) {
                        pos++
                        continue
                    }
                    return null
                }
                else -> {
                    if (currentCommentType == CommentType.MULTI_LINE) {
                        if (peek() == '*' && hasMore(1) && peek(1) == '/') {
                            pos += 2
                            multiLineCommentDepth--
                            currentCommentType = if (multiLineCommentDepth == 0) null else CommentType.MULTI_LINE
                        } else {
                            pos++
                        }
                        continue
                    } else if (currentCommentType == CommentType.SINGLE_LINE) {
                        pos++
                        continue
                    }
                    return null
                }
            }
        }

        if (!hasMore(0) && currentCommentType == CommentType.MULTI_LINE) {
            return ErrorToken(source.substring(commentStart), buildSpan(0))
        }
        return null
    }

    private fun separator(type: SeparatorType): Separator {
        return Separator(type, buildSpan(1))
    }

    private fun lexIdentifierOrKeyword(): Token {
        var off = 1
        while (hasMore(off) && isIdentifierChar(peek(off))) {
            off++
        }
        val id = source.substring(pos, pos + off)
        // This is a naive solution. Using a better data structure (hashmap, trie) likely performs better.
        for (value in KeywordType.values()) {
            if (value.keyword() == id) {
                return Keyword(value, buildSpan(off))
            }
        }
        return Identifier(id, buildSpan(off))
    }

    private fun lexNumber(): Token {
        if (isHexPrefix()) {
            var off = 2
            while (hasMore(off) && isHex(peek(off))) {
                off++
            }
            if (off == 2) {
                // 0x without any further hex digits
                return ErrorToken(source.substring(pos, pos + off), buildSpan(2))
            }
            return NumberLiteral(source.substring(pos, pos + off), 16, buildSpan(off))
        }
        var off = 1
        while (hasMore(off) && isNumeric(peek(off))) {
            off++
        }
        if (peek() == '0' && off > 1) {
            // leading zero is not allowed
            return ErrorToken(source.substring(pos, pos + off), buildSpan(off))
        }
        return NumberLiteral(source.substring(pos, pos + off), 10, buildSpan(off))
    }

    private fun isHexPrefix(): Boolean {
        return peek() == '0' && hasMore(1) && (peek(1) == 'x' || peek(1) == 'X')
    }

    private fun isIdentifierChar(c: Char): Boolean {
        return c == '_' ||
                c in 'a'..'z' ||
                c in 'A'..'Z' ||
                c in '0'..'9'
    }

    private fun isNumeric(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isHex(c: Char): Boolean {
        return isNumeric(c) || c in 'a'..'f' || c in 'A'..'F'
    }

    private fun singleOrAssign(single: OperatorType, assign: OperatorType): Token {
        if (hasMore(1) && peek(1) == '=') {
            return Operator(assign, buildSpan(2))
        }
        return Operator(single, buildSpan(1))
    }

    private fun buildSpan(proceed: Int): Span {
        val start = pos
        pos += proceed
        val s = Position.SimplePosition(line, start - lineStart)
        val e = Position.SimplePosition(line, start - lineStart + proceed)
        return Span.SimpleSpan(s, e)
    }

    private fun peek(): Char {
        return source[pos]
    }

    private fun hasMore(offset: Int): Boolean {
        return pos + offset < source.length
    }

    private fun peek(offset: Int): Char {
        return source[pos + offset]
    }
}