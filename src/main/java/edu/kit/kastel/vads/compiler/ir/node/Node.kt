package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper

/** The base class for all nodes. */
sealed class Node(
    val graph: IrGraph,
    val block: Block?,
    private val predecessors: MutableList<Node> = mutableListOf(),
    private val successors: MutableList<Node> = mutableListOf()
) {
    val debugInfo: DebugInfo = DebugInfoHelper.debugInfo
    val safeBlock: Block = block ?: this as Block

    init {
        predecessors.forEach { predecessor ->
            predecessor.successors.add(this)
        }
    }

    constructor(block: Block, vararg predecessors: Node) : this(
        block.graph, block, predecessors.toMutableList()
    )

    fun predecessors(): List<Node> = predecessors.toList()

    fun successors(): List<Node> = successors.toList()

    fun setPredecessor(idx: Int, node: Node) {
        predecessors[idx].successors.remove(this)
        predecessors[idx] = node
        node.successors.add(this)
    }

    fun addPredecessor(node: Node) {
        predecessors.add(node)
        node.successors.add(this)
    }

    fun predecessor(idx: Int): Node = predecessors[idx]

    override fun toString(): String =
        "${javaClass.simpleName.replace("Node", "")} ${info()}".trimEnd()

    protected open fun info(): String = ""

    companion object {
        internal fun predecessorHash(node: Node, predecessor: Int): Int =
            System.identityHashCode(node.predecessor(predecessor))
    }
}
