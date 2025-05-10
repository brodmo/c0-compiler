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

    override fun visit(returnTree: ReturnTree, data: ReturnState): Unit {
        data.returns = true
        return super.visit(returnTree, data)
    }

    override fun visit(functionTree: FunctionTree, data: ReturnState): Unit {
        if (!data.returns) {
            throw SemanticException("function " + functionTree.name + " does not return")
        }
        data.returns = false
        return super.visit(functionTree, data)
    }
}
