package edu.kit.kastel.vads.compiler.semantic

import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree
import edu.kit.kastel.vads.compiler.parser.visitor.RecursivePostorderVisitor
import edu.kit.kastel.vads.compiler.parser.visitor.Unit
import edu.kit.kastel.vads.compiler.semantic.ReturnAnalysis.ReturnState
import edu.kit.kastel.vads.compiler.semantic.VariableStatusAnalysis.VariableStatus

class SemanticAnalysis(private val program: ProgramTree) {
    fun analyze() {
        this.program.accept<Namespace<Void>, Unit>(
            RecursivePostorderVisitor<Namespace<Void>, Unit>(
                IntegerLiteralRangeAnalysis()
            ), Namespace<Void>()
        )
        this.program.accept<Namespace<VariableStatus>, Unit>(
            RecursivePostorderVisitor<Namespace<VariableStatus>, Unit>(
                VariableStatusAnalysis()
            ), Namespace<VariableStatus>()
        )
        this.program.accept<ReturnState, Unit>(
            RecursivePostorderVisitor<ReturnState, Unit>(ReturnAnalysis()),
            ReturnState()
        )
    }
}
