package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

data class LValueIdentTree(val name: NameTree) : LValueTree {
    override val span = this.name.span

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }
}
