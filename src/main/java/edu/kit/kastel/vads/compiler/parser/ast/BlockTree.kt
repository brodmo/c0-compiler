package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor
import java.util.List

class BlockTree(statements: MutableList<StatementTree>, override val span: Span) : StatementTree {
    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }

    val statements: MutableList<StatementTree>

    init {
        var statements = statements
        statements = List.copyOf<StatementTree>(statements)
        this.statements = statements
    }
}
