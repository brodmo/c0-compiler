package edu.kit.kastel.vads.compiler.parser.visitor

import edu.kit.kastel.vads.compiler.parser.ast.*

/** A visitor that traverses a tree in postorder
 * @param <T> a type for additional data
 * @param <R> a type for a return type
</R></T> */
class RecursivePostorderVisitor<T, R>(private val visitor: Visitor<T, R>) : Visitor<T, R> {
    override fun visit(assignmentTree: AssignmentTree, data: T): R {
        var r = assignmentTree.lValue.accept<T, R>(this, data)
        r = assignmentTree.expression.accept<T, R>(this, accumulate(data, r))
        r = this.visitor.visit(assignmentTree, accumulate(data, r))
        return r
    }

    override fun visit(binaryOperationTree: BinaryOperationTree, data: T): R {
        var r = binaryOperationTree.lhs.accept<T, R>(this, data)
        r = binaryOperationTree.rhs.accept<T, R>(this, accumulate(data, r))
        r = this.visitor.visit(binaryOperationTree, accumulate(data, r))
        return r
    }

    override fun visit(blockTree: BlockTree, data: T): R {
        var r: R
        var d = data
        for (statement in blockTree.statements) {
            r = statement.accept<T, R>(this, d)
            d = accumulate(d, r)
        }
        r = this.visitor.visit(blockTree, d)
        return r
    }

    override fun visit(declarationTree: DeclarationTree, data: T): R {
        var r = declarationTree.type.accept<T, R>(this, data)
        r = declarationTree.name.accept<T, R>(this, accumulate(data, r))
        if (declarationTree.initializer != null) {
            r = declarationTree.initializer.accept<T, R>(this, accumulate(data, r))
        }
        r = this.visitor.visit(declarationTree, accumulate(data, r))
        return r
    }

    override fun visit(functionTree: FunctionTree, data: T): R {
        var r = functionTree.returnType.accept<T, R>(this, data)
        r = functionTree.name.accept<T, R>(this, accumulate(data, r))
        r = functionTree.body.accept<T, R>(this, accumulate(data, r))
        r = this.visitor.visit(functionTree, accumulate(data, r))
        return r
    }

    override fun visit(identExpressionTree: IdentExpressionTree, data: T): R {
        var r = identExpressionTree.name.accept<T, R>(this, data)
        r = this.visitor.visit(identExpressionTree, accumulate(data, r))
        return r
    }

    override fun visit(literalTree: LiteralTree, data: T): R {
        return this.visitor.visit(literalTree, data)
    }

    override fun visit(lValueIdentTree: LValueIdentTree, data: T): R {
        var r = lValueIdentTree.name.accept<T, R>(this, data)
        r = this.visitor.visit(lValueIdentTree, accumulate(data, r))
        return r
    }

    override fun visit(nameTree: NameTree, data: T): R {
        return this.visitor.visit(nameTree, data)
    }

    override fun visit(negateTree: NegateTree, data: T): R {
        var r = negateTree.expression.accept<T, R>(this, data)
        r = this.visitor.visit(negateTree, accumulate(data, r))
        return r
    }

    override fun visit(programTree: ProgramTree, data: T): R {
        var r: R
        var d = data
        for (tree in programTree.topLevelTrees) {
            r = tree.accept<T, R>(this, d)
            d = accumulate(data, r)
        }
        r = this.visitor.visit(programTree, d)
        return r
    }

    override fun visit(returnTree: ReturnTree, data: T): R {
        var r = returnTree.expression.accept<T, R>(this, data)
        r = this.visitor.visit(returnTree, accumulate(data, r))
        return r
    }

    override fun visit(typeTree: TypeTree, data: T): R {
        return this.visitor.visit(typeTree, data)
    }

    protected fun accumulate(data: T, value: R): T {
        return data
    }
}
