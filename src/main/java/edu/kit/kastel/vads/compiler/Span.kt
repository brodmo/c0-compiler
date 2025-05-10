package edu.kit.kastel.vads.compiler

interface Span {
    val start: Position
    val end: Position

    fun merge(later: Span): Span

    data class SimpleSpan(override val start: Position, override val end: Position) : Span {
        override fun merge(later: Span): Span {
            return SimpleSpan(this.start, later.end)
        }

        override fun toString(): String {
            return "[" + this.start + "|" + this.end + "]"
        }
    }
}
