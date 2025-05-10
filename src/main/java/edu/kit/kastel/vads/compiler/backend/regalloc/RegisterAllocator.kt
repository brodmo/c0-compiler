package edu.kit.kastel.vads.compiler.backend.regalloc

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.node.Node

interface RegisterAllocator {
    fun allocateRegisters(graph: IrGraph): MutableMap<Node, Register>
}
