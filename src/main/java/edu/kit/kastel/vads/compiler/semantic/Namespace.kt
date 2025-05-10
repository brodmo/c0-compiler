package edu.kit.kastel.vads.compiler.semantic

import edu.kit.kastel.vads.compiler.parser.ast.NameTree
import edu.kit.kastel.vads.compiler.parser.symbol.Name

class Namespace<T : Any> {
    private val content: MutableMap<Name, T> = mutableMapOf()

    fun put(name: NameTree, value: T, merger: (T, T) -> T) {
        content.merge(name.name, value, merger)
    }

    fun get(name: NameTree): T? {
        return content[name.name]
    }
}