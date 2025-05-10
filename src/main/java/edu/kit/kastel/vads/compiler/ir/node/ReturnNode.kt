package edu.kit.kastel.vads.compiler.ir.node

class ReturnNode(block: Block, sideEffect: Node, result: Node) : Node(block, sideEffect, result) {
    companion object {
        const val SIDE_EFFECT: Int = 0
        const val RESULT: Int = 1
    }
}
