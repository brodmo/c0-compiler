package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.Span.SimpleSpan
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

/**
 * Base interface for all Abstract Syntax Tree (AST) nodes.
 * Provides common properties like source span and a visitor acceptance method.
 */
sealed interface Tree {
    val span: Span

    /**
     * Accepts a visitor to traverse the AST.
     *
     * @param visitor The visitor to accept.
     * @param data Contextual data passed to the visitor.
     * @return The result produced by the visitor.
     */
    fun <T, R> accept(visitor: Visitor<T, R>, data: T): R
}

/**
 * Represents a function definition in the AST.
 */
data class FunctionTree(
    val returnType: TypeTree,
    val name: NameTree,
    val body: BlockTree
) : Tree {
    override val span = SimpleSpan(this.returnType.span.start, this.body.span.end)

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)
}

/**
 * Represents the root of the program's AST.
 * Contains a list of top-level function definitions.
 */
class ProgramTree(topLevelTrees: List<FunctionTree>) : Tree {
    override val span = run {
        // Ensure the program contains at least one function
        require(topLevelTrees.isNotEmpty()) { "Program must contain at least one function" }
        val first: FunctionTree = topLevelTrees.first()
        val last: FunctionTree = topLevelTrees.last()
        SimpleSpan(first.span.start, last.span.end)
    }

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R = visitor.visit(this, data)

    /**
     * The list of top-level function definitions in the program.
     * This list is immutable after construction.
     */
    val topLevelTrees: List<FunctionTree> = topLevelTrees.toList()
}
