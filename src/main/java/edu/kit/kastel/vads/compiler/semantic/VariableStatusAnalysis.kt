package edu.kit.kastel.vads.compiler.semantic

import edu.kit.kastel.vads.compiler.lexer.Operator
import edu.kit.kastel.vads.compiler.lexer.OperatorType
import edu.kit.kastel.vads.compiler.parser.ast.*
import edu.kit.kastel.vads.compiler.parser.visitor.NoOpVisitor
import edu.kit.kastel.vads.compiler.parser.visitor.Unit
import java.util.*

/**
 * Checks that variables are
 * - declared before assignment
 * - not declared twice
 * - not initialized twice
 * - assigned before referenced
 */
class VariableStatusAnalysis : NoOpVisitor<Namespace<VariableStatusAnalysis.VariableStatus>> {

    override fun visit(assignmentTree: AssignmentTree, context: Namespace<VariableStatus>): Unit {
        when (val lValue = assignmentTree.lValue) {
            is LValueIdentTree -> {
                val name = lValue.name
                val status = context.get(name)
                when (assignmentTree.operator.type) {
                    OperatorType.ASSIGN -> {
                        checkDeclared(name, status)
                    }

                    else -> {
                        checkInitialized(name, status)
                    }
                }
                if (status != VariableStatus.INITIALIZED) {
                    // only update when needed, reassignment is totally fine
                    updateStatus(context, VariableStatus.INITIALIZED, name)
                }
            }
        }
        return super.visit(assignmentTree, context)
    }

    override fun visit(declarationTree: DeclarationTree, context: Namespace<VariableStatus>): Unit {
        checkUndeclared(declarationTree.name, context.get(declarationTree.name))
        val status = if (declarationTree.initializer == null) {
            VariableStatus.DECLARED
        } else {
            VariableStatus.INITIALIZED
        }
        updateStatus(context, status, declarationTree.name)
        return super.visit(declarationTree, context)
    }

    override fun visit(identExpressionTree: IdentExpressionTree, context: Namespace<VariableStatus>): Unit {
        val status = context.get(identExpressionTree.name)
        checkInitialized(identExpressionTree.name, status)
        return super.visit(identExpressionTree, context)
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

        private fun updateStatus(context: Namespace<VariableStatus>, status: VariableStatus, name: NameTree) {
            context.put(name, status) { existing, replacement ->
                if (existing.ordinal >= replacement.ordinal) {
                    throw SemanticException("variable is already $existing. Cannot be $replacement here.")
                }
                replacement
            }
        }
    }
}