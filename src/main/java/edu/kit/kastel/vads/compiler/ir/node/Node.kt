package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper

sealed class Node(
    val graph: IrGraph,
    val block: Block?,
    originalPredecessors: List<Node>
) {
    open val predecessors: List<Node> = originalPredecessors
    val successors: MutableList<Node> = mutableListOf()
    val debugInfo: DebugInfo = DebugInfoHelper.debugInfo

    init {
        originalPredecessors.forEach { it.successors.add(this) }
    }

    constructor(block: Block, predecessors: List<Node>) : this(
        block.graph,
        block,
        predecessors
    )

    abstract fun <T> accept(visitor: NodeVisitor<T>): T

    open fun hasSideEffect(): Boolean = false

    open fun skipProj(): Node = this

    override fun toString(): String = listOfNotNull(this::class.simpleName, info()).joinToString(" ")

    protected open fun info(): String? = null
}
