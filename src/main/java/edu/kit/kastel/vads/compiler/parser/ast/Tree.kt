package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

interface Tree {
    val span: Span

    fun <T, R> accept(visitor: Visitor<T, R>, data: T): R
}
