package edu.kit.kastel.vads.compiler

interface Position {
    val line: Int
    val column: Int

    data class SimplePosition(override val line: Int, override val column: Int) : Position {
        override fun toString(): String {
            return this.line.toString() + ":" + this.column
        }
    }
}
