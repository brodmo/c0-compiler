package edu.kit.kastel.vads.compiler.ir.node

class Phi(block: Block) : Node(block) {
    fun appendOperand(node: Node) {
        addPredecessor(node)
    }
}
