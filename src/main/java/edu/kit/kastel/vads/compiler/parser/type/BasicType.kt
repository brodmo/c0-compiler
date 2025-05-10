package edu.kit.kastel.vads.compiler.parser.type

import java.util.*

enum class BasicType : Type {
    INT;

    override fun asString(): String {
        return name.lowercase(Locale.ROOT)
    }
}
