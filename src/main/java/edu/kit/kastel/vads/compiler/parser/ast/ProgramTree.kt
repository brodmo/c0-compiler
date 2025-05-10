package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span.SimpleSpan
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor
import java.util.List

class ProgramTree(topLevelTrees: MutableList<FunctionTree>) : Tree {
    override val span = run {
        val first: FunctionTree = topLevelTrees.first()
        val last: FunctionTree = topLevelTrees.last()
        SimpleSpan(first.span.start, last.span.end)
    }

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }

    val topLevelTrees: MutableList<FunctionTree>

    init {
        var topLevelTrees = topLevelTrees
        assert(!topLevelTrees.isEmpty()) { "must be non-empty" }
        topLevelTrees = List.copyOf<FunctionTree>(topLevelTrees)
        this.topLevelTrees = topLevelTrees
    }
}
