package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper

sealed class Node(
    val graph: IrGraph,
    val block: Block?,
    originalPredecessors: List<Node> = listOf()
) {
    // TODO add code generation method returning (Register (Up), List<Instructions> (Down))
    // TODO maybe just switch case in InstructionSelector
    // TODO don't pass target register, optimizing register usage is part of register allocation
    val successors: MutableList<Node> = mutableListOf()
    open val predecessors: List<Node> = originalPredecessors
    val safeBlock: Block = block ?: this as Block
    val debugInfo: DebugInfo = DebugInfoHelper.debugInfo

    init {
        // The only possible causes of an NPE in Kotlin are: [...]
        // - A superclass constructor calling an open member whose implementation in the derived class uses an uninitialized state.
        // Hence we cannot declare predecessors open
        originalPredecessors.forEach { predecessor ->
            predecessor.successors.add(this)
        }
    }

    constructor(block: Block, predecessors: List<Node> = listOf()) : this(
        block.graph,
        block,
        predecessors
    )

    open fun skipProj(): Node = this

    override fun toString(): String = listOfNotNull(this::class.simpleName, info()).joinToString(" ")

    protected open fun info(): String? = null
}
