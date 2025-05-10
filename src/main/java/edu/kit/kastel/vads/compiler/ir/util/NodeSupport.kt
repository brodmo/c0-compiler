package edu.kit.kastel.vads.compiler.ir.util

import edu.kit.kastel.vads.compiler.ir.node.Node
import edu.kit.kastel.vads.compiler.ir.node.ProjNode

object NodeSupport {
    fun predecessorSkipProj(node: Node, predIdx: Int): Node {
        val pred = node.predecessor(predIdx)
        if (pred is ProjNode) {
            return pred.predecessor(ProjNode.Companion.IN)
        }
        return pred
    }
}
