package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo.NoInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper

/** The base class for all nodes. */
sealed class Node {
    val graph: IrGraph
    val block: Block
    private val predecessors: MutableList<Node> = mutableListOf()
    val debugInfo: DebugInfo

    protected constructor(block: Block, vararg predecessors: Node) {
        this.graph = block.graph
        this.block = block
        this.predecessors.addAll(predecessors)
        for (predecessor in predecessors) {
            graph.registerSuccessor(predecessor, this)
        }
        this.debugInfo = DebugInfoHelper.debugInfo
    }

    protected constructor(graph: IrGraph) {
        assert(this.javaClass == Block::class.java) { "must be used by Block only" }
        this.graph = graph
        this.block = this as Block
        this.debugInfo = NoInfo.INSTANCE
    }

    fun predecessors(): MutableList<out Node> {
        return this.predecessors.toMutableList()
    }

    fun setPredecessor(idx: Int, node: Node) {
        this.graph.removeSuccessor(this.predecessors[idx], this)
        this.predecessors[idx] = node
        this.graph.registerSuccessor(node, this)
    }

    fun addPredecessor(node: Node) {
        this.predecessors.add(node)
        this.graph.registerSuccessor(node, this)
    }

    fun predecessor(idx: Int): Node {
        return this.predecessors[idx]
    }

    override fun toString(): String {
        return (this.javaClass.getSimpleName().replace("Node", "") + " " + info()).trimEnd()
    }

    protected open fun info(): String {
        return ""
    }

    companion object {
        internal fun predecessorHash(node: Node, predecessor: Int): Int {
            return System.identityHashCode(node.predecessor(predecessor))
        }
    }
}
