package edu.kit.kastel.vads.compiler.parser.visitor

import edu.kit.kastel.vads.compiler.parser.ast.*

/** A visitor that traverses a tree in postorder
 * @param <T> a type for additional context
 * @param <R> a type for a return type
</R></T> */
class RecursivePostorderVisitor<T, R>(private val visitor: Visitor<T, R>) : Visitor<T, R> {
    override fun visit(assignmentTree: AssignmentTree, context: T): R {
        var r = assignmentTree.lValue.accept<T, R>(this, context)
        r = assignmentTree.expression.accept<T, R>(this, accumulate(context, r))
        r = this.visitor.visit(assignmentTree, accumulate(context, r))
        return r
    }

    override fun visit(binaryOperationTree: BinaryOperationTree, context: T): R {
        var r = binaryOperationTree.lhs.accept<T, R>(this, context)
        r = binaryOperationTree.rhs.accept<T, R>(this, accumulate(context, r))
        r = this.visitor.visit(binaryOperationTree, accumulate(context, r))
        return r
    }

    override fun visit(blockTree: BlockTree, context: T): R {
        var r: R
        var d = context
        for (statement in blockTree.statements) {
            r = statement.accept<T, R>(this, d)
            d = accumulate(d, r)
        }
        r = this.visitor.visit(blockTree, d)
        return r
    }

    override fun visit(declarationTree: DeclarationTree, context: T): R {
        var r = declarationTree.type.accept<T, R>(this, context)
        r = declarationTree.name.accept<T, R>(this, accumulate(context, r))
        if (declarationTree.initializer != null) {
            r = declarationTree.initializer.accept<T, R>(this, accumulate(context, r))
        }
        r = this.visitor.visit(declarationTree, accumulate(context, r))
        return r
    }

    override fun visit(functionTree: FunctionTree, context: T): R {
        var r = functionTree.returnType.accept<T, R>(this, context)
        r = functionTree.name.accept<T, R>(this, accumulate(context, r))
        r = functionTree.body.accept<T, R>(this, accumulate(context, r))
        r = this.visitor.visit(functionTree, accumulate(context, r))
        return r
    }

    override fun visit(identExpressionTree: IdentExpressionTree, context: T): R {
        var r = identExpressionTree.name.accept<T, R>(this, context)
        r = this.visitor.visit(identExpressionTree, accumulate(context, r))
        return r
    }

    override fun visit(literalTree: LiteralTree, context: T): R {
        return this.visitor.visit(literalTree, context)
    }

    override fun visit(lValueIdentTree: LValueIdentTree, context: T): R {
        var r = lValueIdentTree.name.accept<T, R>(this, context)
        r = this.visitor.visit(lValueIdentTree, accumulate(context, r))
        return r
    }

    override fun visit(nameTree: NameTree, context: T): R {
        return this.visitor.visit(nameTree, context)
    }

    override fun visit(negateTree: NegateTree, context: T): R {
        var r = negateTree.expression.accept<T, R>(this, context)
        r = this.visitor.visit(negateTree, accumulate(context, r))
        return r
    }

    override fun visit(programTree: ProgramTree, context: T): R {
        var r: R
        var d = context
        for (tree in programTree.topLevelTrees) {
            r = tree.accept<T, R>(this, d)
            d = accumulate(context, r)
        }
        r = this.visitor.visit(programTree, d)
        return r
    }

    override fun visit(returnTree: ReturnTree, context: T): R {
        var r = returnTree.expression.accept<T, R>(this, context)
        r = this.visitor.visit(returnTree, accumulate(context, r))
        return r
    }

    override fun visit(typeTree: TypeTree, context: T): R {
        return this.visitor.visit(typeTree, context)
    }

    protected fun accumulate(context: T, value: R): T {
        return context
    }
}
