package edu.kit.kastel.vads.compiler.semantic

import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree
import edu.kit.kastel.vads.compiler.parser.ast.ReturnTree
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor
import edu.kit.kastel.vads.compiler.parser.visitor.Unit
import edu.kit.kastel.vads.compiler.semantic.ReturnAnalysis.ReturnState

/** Checks that functions return.
 * Currently only works for straight-line code. */
internal class ReturnAnalysis : NoOpVisitor<ReturnState> {
    internal class ReturnState {
        var returns: Boolean = false
    }

    override fun visit(returnTree: ReturnTree, context: ReturnState): Unit {
        context.returns = true
        return super.visit(returnTree, context)
    }

    override fun visit(functionTree: FunctionTree, context: ReturnState): Unit {
        if (!context.returns) {
            throw SemanticException("function " + functionTree.name + " does not return")
        }
        context.returns = false
        return super.visit(functionTree, context)
    }
}
