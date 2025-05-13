package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph

sealed class OrderNode(graph: IrGraph, block: Block?) : Node(graph, block) {
    override val predecessors: MutableList<Node> = super.predecessors.toMutableList()
    fun addPredecessor(node: Node) {
        predecessors.add(node)
        node.successors.add(this)
    }

}

class Block(graph: IrGraph) : OrderNode(graph, null)

class Phi(block: Block) : OrderNode(block.graph, block)
