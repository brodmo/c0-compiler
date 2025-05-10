package edu.kit.kastel.vads.compiler.ir

import edu.kit.kastel.vads.compiler.ir.node.Block
import edu.kit.kastel.vads.compiler.ir.node.Node
import java.util.*
import java.util.Set
import java.util.function.Function

class IrGraph(private val name: String) {
    private val successors: MutableMap<Node, SequencedSet<Node>> = IdentityHashMap<Node, SequencedSet<Node>>()
    private val startBlock: Block
    private val endBlock: Block

    init {
        this.startBlock = Block(this)
        this.endBlock = Block(this)
    }

    fun registerSuccessor(node: Node, successor: Node) {
        this.successors.computeIfAbsent(node, Function { `_`: Node -> LinkedHashSet() }).add(successor)
    }

    fun removeSuccessor(node: Node, oldSuccessor: Node) {
        this.successors.computeIfAbsent(node, Function { `_`: Node -> LinkedHashSet() }).remove(oldSuccessor)
    }

    /** {@return the set of nodes that have the given node as one of their inputs} */
    fun successors(node: Node): MutableSet<Node> {
        val successors = this.successors.get(node)
        if (successors == null) {
            return Set.of<Node>()
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
