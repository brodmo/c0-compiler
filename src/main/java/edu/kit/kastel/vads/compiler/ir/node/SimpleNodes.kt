package edu.kit.kastel.vads.compiler.ir.node

import edu.kit.kastel.vads.compiler.ir.IrGraph


class StartNode(block: Block) : Node(block)

class ReturnNode(block: Block, val sideEffect: Node, val result: Node) : Node(block, mutableListOf(sideEffect, result))

class ProjNode(block: Block, val pred: Node, val projectionInfo: ProjectionInfo) : Node(block, mutableListOf(pred)) {
    override fun skipProj(): Node = pred
    override fun info(): String = projectionInfo.name
}

enum class ProjectionInfo {
    RESULT, SIDE_EFFECT
}

class ConstIntNode(block: Block, val value: Int) : Node(block) {
    override fun info(): String = "[$value]"

    override fun equals(other: Any?): Boolean =
        other is ConstIntNode && safeBlock == other.safeBlock && value == other.value

    override fun hashCode(): Int = value
}

sealed class MutablePredecessorsNode(graph: IrGraph, block: Block?) : Node(graph, block) {
    fun addPredecessor(node: Node) {
        predecessors.add(node)
        node.successors.add(this)
    }

}

class Block(graph: IrGraph) : MutablePredecessorsNode(graph, null)

class Phi(block: Block) : MutablePredecessorsNode(block.graph, block)
