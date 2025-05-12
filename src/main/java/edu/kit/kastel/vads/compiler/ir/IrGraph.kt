package edu.kit.kastel.vads.compiler.ir

import edu.kit.kastel.vads.compiler.ir.node.Block
import edu.kit.kastel.vads.compiler.ir.node.Node
import java.util.*

class IrGraph(val name: String) {
    private val successors: MutableMap<Node, SequencedSet<Node>> = mutableMapOf()
    val startBlock: Block = Block(this)
    val endBlock: Block = Block(this)

    fun registerSuccessor(node: Node, successor: Node) {
        successors.getOrPut(node) { linkedSetOf() }.add(successor)
    }

    fun removeSuccessor(node: Node, oldSuccessor: Node) {
        successors.getOrPut(node) { linkedSetOf() }.remove(oldSuccessor)
    }

    /** {@return the set of nodes that have the given node as one of their inputs} */
    fun successors(node: Node): Set<Node> {
        return successors[node]?.toSet() ?: emptySet()
    }
}
