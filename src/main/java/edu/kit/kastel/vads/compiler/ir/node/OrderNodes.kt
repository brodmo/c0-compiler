package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph

sealed class OrderNode(graph: IrGraph, block: Block?) : Node(graph, block, emptyList()) {
    override val predecessors: MutableList<Node> = super.predecessors.toMutableList()
    fun addPredecessor(node: Node) {
        predecessors.add(node)
        node.successors.add(this)
    }
}

class Block(graph: IrGraph) : OrderNode(graph, null) {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

class Phi(block: Block) : OrderNode(block.graph, block) {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
