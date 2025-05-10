package edu.kit.kastel.vads.compiler.semantic

import edu.kit.kastel.vads.compiler.lexer.Operator
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree
import edu.kit.kastel.vads.compiler.parser.ast.NameTree
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor
import edu.kit.kastel.vads.compiler.parser.visitor.Unit
import java.util.Locale

/**
 * Checks that variables are
 * - declared before assignment
 * - not declared twice
 * - not initialized twice
 * - assigned before referenced
 */
class VariableStatusAnalysis : NoOpVisitor<Namespace<VariableStatusAnalysis.VariableStatus>> {

    override fun visit(assignmentTree: AssignmentTree, data: Namespace<VariableStatus>): Unit {
        when (val lValue = assignmentTree.lValue) {
            is LValueIdentTree -> {
                val name = lValue.name
                val status = data.get(name)
                when (assignmentTree.operator.type) {
                    Operator.OperatorType.ASSIGN -> {
                        checkDeclared(name, status)
                    }
                    else -> {
                        checkInitialized(name, status)
                    }
                }
                if (status != VariableStatus.INITIALIZED) {
                    // only update when needed, reassignment is totally fine
                    updateStatus(data, VariableStatus.INITIALIZED, name)
                }
            }
        }
        return super.visit(assignmentTree, data)
    }

    override fun visit(declarationTree: DeclarationTree, data: Namespace<VariableStatus>): Unit {
        checkUndeclared(declarationTree.name, data.get(declarationTree.name))
        val status = if (declarationTree.initializer == null) {
            VariableStatus.DECLARED
        } else {
            VariableStatus.INITIALIZED
        }
        updateStatus(data, status, declarationTree.name)
        return super.visit(declarationTree, data)
    }

    override fun visit(identExpressionTree: IdentExpressionTree, data: Namespace<VariableStatus>): Unit {
        val status = data.get(identExpressionTree.name)
        checkInitialized(identExpressionTree.name, status)
        return super.visit(identExpressionTree, data)
    }

    enum class VariableStatus {
        DECLARED,
        INITIALIZED;

        override fun toString(): String {
            return name.lowercase(Locale.ROOT)
        }
    }

    companion object {
        private fun checkDeclared(name: NameTree, status: VariableStatus?) {
            if (status == null) {
                throw SemanticException("Variable $name must be declared before assignment")
            }
        }

        private fun checkInitialized(name: NameTree, status: VariableStatus?) {
            if (status == null || status == VariableStatus.DECLARED) {
                throw SemanticException("Variable $name must be initialized before use")
            }
        }

        private fun checkUndeclared(name: NameTree, status: VariableStatus?) {
            if (status != null) {
                throw SemanticException("Variable $name is already declared")
            }
        }

        private fun updateStatus(data: Namespace<VariableStatus>, status: VariableStatus, name: NameTree) {
            data.put(name, status) { existing, replacement ->
                if (existing.ordinal >= replacement.ordinal) {
                    throw SemanticException("variable is already $existing. Cannot be $replacement here.")
                }
                replacement
            }
        }
    }
}