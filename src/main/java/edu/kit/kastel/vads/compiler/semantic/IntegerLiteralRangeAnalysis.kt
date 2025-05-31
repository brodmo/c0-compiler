package edu.kit.kastel.vads.compiler.semantic

import edu.kit.kastel.vads.compiler.parser.ast.LiteralTree
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor
import edu.kit.kastel.vads.compiler.parser.visitor.Unit

class IntegerLiteralRangeAnalysis : NoOpVisitor<Namespace<Void>> {
    override fun visit(literalTree: LiteralTree, context: Namespace<Void>): Unit {
        literalTree.parseValue() ?: throw SemanticException("invalid integer literal " + literalTree.value)
        return super.visit(literalTree, context)
    }
}
