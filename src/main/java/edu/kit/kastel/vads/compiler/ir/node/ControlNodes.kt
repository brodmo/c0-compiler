package edu.kit.kastel.vads.compiler.ir.node

class StartNode(block: Block) : Node(block, emptyList()) {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

class ReturnNode(block: Block, val sideEffect: Node, val result: Node) : Node(block, listOf(sideEffect, result)) {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

class ProjNode(block: Block, val pred: Node, val projectionInfo: ProjectionInfo) : Node(block, listOf(pred)) {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
    override fun skipProj(): Node = pred
    override fun info(): String = projectionInfo.name
}

enum class ProjectionInfo {
    RESULT, SIDE_EFFECT
}
