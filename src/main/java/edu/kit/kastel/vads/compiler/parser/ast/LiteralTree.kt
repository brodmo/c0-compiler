package edu.kit.kastel.vads.compiler.parser.ast

import edu.kit.kastel.vads.compiler.Span
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor
import java.util.*
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.NumberFormatException
import kotlin.String

@JvmRecord
data class LiteralTree(val value: String, val base: Int, override val span: Span) : ExpressionTree {
    override fun <T, R> accept(visitor: Visitor<T, R>, data: T): R {
        return visitor.visit(this, data)
    }

    fun parseValue(): OptionalLong {
        val end: Int = value.length
        return when (base) {
            16 -> parseHex(end)
            10 -> parseDec(end)
            else -> throw IllegalArgumentException("unexpected base " + base)
        }
    }

    private fun parseDec(end: Int): OptionalLong {
        val l: Long
        try {
            l = value.substring(0, end).toLong(base)
        } catch (`_`: NumberFormatException) {
            return OptionalLong.empty()
        }
        if (l < 0 || l > Integer.toUnsignedLong(Integer.MIN_VALUE)) {
            return OptionalLong.empty()
        }
        return OptionalLong.of(l)
    }

    private fun parseHex(end: Int): OptionalLong {
        try {
            return OptionalLong.of(Integer.parseUnsignedInt(value, 2, end, 16).toLong())
        } catch (e: NumberFormatException) {
            return OptionalLong.empty()
        }
    }
}
