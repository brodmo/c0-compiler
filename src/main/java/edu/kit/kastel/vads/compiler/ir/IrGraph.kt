package edu.kit.kastel.vads.compiler.ir

import edu.kit.kastel.vads.compiler.ir.node.Block

class IrGraph(val name: String) {
    val startBlock: Block = Block(this)
    val endBlock: Block = Block(this)
}
