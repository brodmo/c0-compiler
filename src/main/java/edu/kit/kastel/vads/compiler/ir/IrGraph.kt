package edu.kit.kastel.vads.compiler.ir

import edu.kit.kastel.vads.compiler.ir.node.Block
import edu.kit.kastel.vads.compiler.ir.node.Node
import java.util.*
import java.util.Set
import java.util.function.Function

class IrGraph(private val name: String) {
    private val successors: MutableMap<Node, SequencedSet<Node>> = mutableMapOf()
    private val startBlock: Block = Block(this)
    private val endBlock: Block = Block(this)

    fun registerSuccessor(node: Node, successor: Node) {
        this.successors.computeIfAbsent(node, Function { `_`: Node -> LinkedHashSet() }).add(successor)
    }

    fun removeSuccessor(node: Node, oldSuccessor: Node) {
        this.successors.computeIfAbsent(node, Function { `_`: Node -> LinkedHashSet() }).remove(oldSuccessor)
    }

    /** {@return the set of nodes that have the given node as one of their inputs} */
    fun successors(node: Node): MutableSet<Node> {
        val successors = this.successors[node]
        if (successors == null) {
            return mutableSetOf<Node>()
        }
        return Set.copyOf<Node>(successors)
    }

    fun startBlock(): Block {
        return this.startBlock
    }

    fun endBlock(): Block {
        return this.endBlock
    }

    /** {@return the name of this graph} */
    fun name(): String {
        return name
    }
}
