package edu.kit.kastel.vads.compiler.parser.symbol

@JvmRecord
internal data class IdentName(val identifier: String) : Name {
    override fun asString(): String {
        return this.identifier
    }
}
