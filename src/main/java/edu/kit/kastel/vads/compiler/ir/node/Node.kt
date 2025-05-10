package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo.NoInfo
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper
import java.util.List

/** The base class for all nodes. */
abstract class Node {
    private val graph: IrGraph
    private val block: Block
    private val predecessors: MutableList<Node> = ArrayList<Node>()
    private val debugInfo: DebugInfo

    protected constructor(block: Block, vararg predecessors: Node) {
        this.graph = block.graph()
        this.block = block
        this.predecessors.addAll(List.of<Node>(*predecessors))
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

    fun graph(): IrGraph {
        return this.graph
    }

    fun block(): Block {
        return this.block
    }

    fun predecessors(): MutableList<out Node> {
        return List.copyOf<Node>(this.predecessors)
    }

    fun setPredecessor(idx: Int, node: Node) {
        this.graph.removeSuccessor(this.predecessors.get(idx), this)
        this.predecessors.set(idx, node)
        this.graph.registerSuccessor(node, this)
    }

    fun addPredecessor(node: Node) {
        this.predecessors.add(node)
        this.graph.registerSuccessor(node, this)
    }

    fun predecessor(idx: Int): Node {
        return this.predecessors.get(idx)
    }

    override fun toString(): String {
        return (this.javaClass.getSimpleName().replace("Node", "") + " " + info()).trimEnd()
    }

    protected open fun info(): String {
        return ""
    }

    fun debugInfo(): DebugInfo {
        return debugInfo
    }

    companion object {
        @JvmStatic
        protected fun predecessorHash(node: Node, predecessor: Int): Int {
            return System.identityHashCode(node.predecessor(predecessor))
        }
    }
}
