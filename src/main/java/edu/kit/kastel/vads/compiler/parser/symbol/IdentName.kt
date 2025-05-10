package edu.kit.kastel.vads.compiler.parser.symbol

internal data class IdentName(val identifier: String) : Name {
    override fun asString(): String {
        return this.identifier
    }
}
