package edu.kit.kastel.vads.compiler.parser

import edu.kit.kastel.vads.compiler.lexer.*
import edu.kit.kastel.vads.compiler.parser.ast.*
import edu.kit.kastel.vads.compiler.parser.symbol.Name
import edu.kit.kastel.vads.compiler.parser.type.BasicType

class Parser(private val tokenSource: TokenSource) {

    fun parseProgram(): ProgramTree {
        val programTree = ProgramTree(mutableListOf(parseFunction()))
        if (tokenSource.hasMore()) {
            throw ParseException("expected end of input but got ${tokenSource.peek()}")
        }
        return programTree
    }

    private fun parseFunction(): FunctionTree {
        val returnType = tokenSource.expectKeyword(KeywordType.INT)
        val identifier = tokenSource.expectIdentifier()
        if (identifier.value != "main") {
            throw ParseException("expected main function but got $identifier");
        }
        tokenSource.expectSeparator(SeparatorType.PAREN_OPEN)
        tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE)
        val body = parseBlock()
        return FunctionTree(
            TypeTree(BasicType.INT, returnType.span),
            name(identifier),
            body
        )
    }

    private fun parseBlock(): BlockTree {
        val bodyOpen = tokenSource.expectSeparator(SeparatorType.BRACE_OPEN)
        val statements = mutableListOf<StatementTree>()
        while (!(tokenSource.peek() is Separator && (tokenSource.peek() as Separator).type == SeparatorType.BRACE_CLOSE)) {
            statements.add(parseStatement())
        }
        val bodyClose = tokenSource.expectSeparator(SeparatorType.BRACE_CLOSE)
        return BlockTree(statements, bodyOpen.span.merge(bodyClose.span))
    }

    private fun parseStatement(): StatementTree {
        val statement = when {
            tokenSource.peek().isKeyword(KeywordType.INT) -> parseDeclaration()
            tokenSource.peek().isKeyword(KeywordType.RETURN) -> parseReturn()
            else -> parseSimple()
        }
        tokenSource.expectSeparator(SeparatorType.SEMICOLON)
        return statement
    }

    private fun parseDeclaration(): StatementTree {
        val type = tokenSource.expectKeyword(KeywordType.INT)
        val ident = tokenSource.expectIdentifier()
        var expr: ExpressionTree? = null
        if (tokenSource.peek().isOperator(OperatorType.ASSIGN)) {
            tokenSource.expectOperator(OperatorType.ASSIGN)
            expr = parseExpression()
        }
        return DeclarationTree(TypeTree(BasicType.INT, type.span), name(ident), expr)
    }

    private fun parseSimple(): StatementTree {
        val lValue = parseLValue()
        val assignmentOperator = parseAssignmentOperator()
        val expression = parseExpression()
        return AssignmentTree(lValue, assignmentOperator, expression)
    }

    private fun parseAssignmentOperator(): Operator {
        val peek = tokenSource.peek()
        if (peek is Operator) {
            return when (peek.type) {
                OperatorType.ASSIGN,
                OperatorType.ASSIGN_DIV,
                OperatorType.ASSIGN_MINUS,
                OperatorType.ASSIGN_MOD,
                OperatorType.ASSIGN_MUL,
                OperatorType.ASSIGN_PLUS -> {
                    tokenSource.consume()
                    peek
                }

                else -> throw ParseException("expected assignment but got ${peek.type}")
            }
        }
        throw ParseException("expected assignment but got $peek")
    }

    private fun parseLValue(): LValueTree {
        if (tokenSource.peek().isSeparator(SeparatorType.PAREN_OPEN)) {
            tokenSource.expectSeparator(SeparatorType.PAREN_OPEN)
            val inner = parseLValue()
            tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE)
            return inner
        }
        val identifier = tokenSource.expectIdentifier()
        return LValueIdentTree(name(identifier))
    }

    private fun parseReturn(): StatementTree {
        val ret = tokenSource.expectKeyword(KeywordType.RETURN)
        val expression = parseExpression()
        return ReturnTree(expression, ret.span.start)
    }

    private fun parseExpression(): ExpressionTree {
        var lhs = parseTerm()
        while (true) {
            val peek = tokenSource.peek()
            if (peek is Operator && (peek.type == OperatorType.PLUS || peek.type == OperatorType.MINUS)) {
                tokenSource.consume()
                lhs = BinaryOperationTree(lhs, parseTerm(), peek.type)
            } else {
                return lhs
            }
        }
    }

    private fun parseTerm(): ExpressionTree {
        var lhs = parseFactor()
        while (true) {
            val peek = tokenSource.peek()
            if (peek is Operator && (peek.type == OperatorType.MUL || peek.type == OperatorType.DIV || peek.type == OperatorType.MOD)) {
                tokenSource.consume()
                lhs = BinaryOperationTree(lhs, parseFactor(), peek.type)
            } else {
                return lhs
            }
        }
    }

    private fun parseFactor(): ExpressionTree {
        return when (val peek = tokenSource.peek()) {
            is Separator -> when (peek.type) {
                SeparatorType.PAREN_OPEN -> {
                    tokenSource.consume()
                    val expression = parseExpression()
                    tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE)
                    expression
                }

                else -> throw ParseException("invalid factor $peek")
            }

            is Operator -> when (peek.type) {
                OperatorType.MINUS -> {
                    val span = tokenSource.consume().span
                    NegateTree(parseFactor(), span)
                }

                else -> throw ParseException("invalid factor $peek")
            }

            is Identifier -> {
                tokenSource.consume()
                IdentExpressionTree(name(peek))
            }

            is NumberLiteral -> {
                tokenSource.consume()
                LiteralTree(peek.value, peek.base, peek.span)
            }

            else -> throw ParseException("invalid factor $peek")
        }
    }

    companion object {
        private fun name(ident: Identifier): NameTree {
            return NameTree(Name.forIdentifier(ident), ident.span)
        }
    }
}