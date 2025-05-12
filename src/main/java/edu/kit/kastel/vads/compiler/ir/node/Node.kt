package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper

sealed class Node(
    val graph: IrGraph,
    val block: Block?,
    val predecessors: MutableList<Node> = mutableListOf()
) {
    val successors: MutableList<Node> = mutableListOf()
    val safeBlock: Block = block ?: this as Block
    val debugInfo: DebugInfo = DebugInfoHelper.debugInfo

    init {
        // The only possible causes of an NPE in Kotlin are: [...]
        // - A superclass constructor calling an open member whose implementation in the derived class uses an uninitialized state.
        // Hence we cannot declare predecessors open
        predecessors.forEach { predecessor ->
            predecessor.successors.add(this)
        }
    }

    constructor(block: Block, predecessors: MutableList<Node> = mutableListOf()) : this(
        block.graph,
        block,
        predecessors
    )

    open fun skipProj(): Node = this

    override fun toString(): String = listOfNotNull(this::class.simpleName, info()).joinToString(" ")

    protected open fun info(): String? = null
}
