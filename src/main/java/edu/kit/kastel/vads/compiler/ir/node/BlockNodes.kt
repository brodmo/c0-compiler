package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph

sealed class BlockNode(graph: IrGraph, block: Block?) : Node(graph, block, emptyList()) {
    override val predecessors: MutableList<Node> = super.predecessors.toMutableList()
    fun addPredecessor(node: Node) {
        predecessors.add(node)
        node.successors.add(this)
    }
}

class Block(graph: IrGraph) : BlockNode(graph, null) {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

class Phi(block: Block) : BlockNode(block.graph, block) {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
