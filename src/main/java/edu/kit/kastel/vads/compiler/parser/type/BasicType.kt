package edu.kit.kastel.vads.compiler.parser.type

import java.util.*

enum class BasicType : Type {
    INT;

    override fun asString(): String = name.lowercase(Locale.ROOT)
}
