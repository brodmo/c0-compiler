package edu.kit.kastel.vads.compiler.semantic

import edu.kit.kastel.vads.compiler.parser.ast.LiteralTree
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor
import edu.kit.kastel.vads.compiler.parser.visitor.Unit
import java.util.function.Supplier

class IntegerLiteralRangeAnalysis : NoOpVisitor<Namespace<Void>> {
    override fun visit(literalTree: LiteralTree, data: Namespace<Void>): Unit {
        literalTree.parseValue()
            .orElseThrow<SemanticException>(
                Supplier { SemanticException("invalid integer literal " + literalTree.value) }
            )
        return super.visit(literalTree, data)
    }
}
