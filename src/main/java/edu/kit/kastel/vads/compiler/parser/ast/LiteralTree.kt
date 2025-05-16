package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor

data class LiteralTree(
    val value: String,
    val base: Int,
    override val span: Span
) : ExpressionTree {

    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }

    fun parseValue(): Long? {
        val end = value.length
        return when (base) {
            16 -> parseHex(end)
            10 -> parseDec(end)
            else -> throw IllegalArgumentException("unexpected base $base")
        }
    }

    private fun parseDec(end: Int): Long? {
        return try {
            val l = value.substring(0, end).toLong(base)
            if (l < 0 || l > Integer.MIN_VALUE.toUInt().toLong()) {
                null
            } else {
                l
            }
        } catch (`_`: NumberFormatException) {
            null
        }
    }

    private fun parseHex(end: Int): Long? {
        return try {
            value.substring(2, end).toUInt(16).toLong()
        } catch (`_`: NumberFormatException) {
            null
        }
    }
}
