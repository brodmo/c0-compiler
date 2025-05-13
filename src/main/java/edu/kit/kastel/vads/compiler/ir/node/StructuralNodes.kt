package edu.kit.kastel.vads.compiler.ir.node

class StartNode(block: Block) : Node(block)

class ReturnNode(block: Block, val sideEffect: Node, val result: Node) : Node(block, listOf(sideEffect, result))

class ProjNode(block: Block, val pred: Node, val projectionInfo: ProjectionInfo) : Node(block, listOf(pred)) {
    override fun skipProj(): Node = pred
    override fun info(): String = projectionInfo.name
}

enum class ProjectionInfo {
    RESULT, SIDE_EFFECT
}
