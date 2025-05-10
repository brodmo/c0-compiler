package edu.kit.kastel.vads.compiler.backend.aasm

import edu.kit.kastel.vads.compiler.backend.regalloc.Register
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator
import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.node.*
import java.util.Map
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet

class AasmRegisterAllocator : RegisterAllocator {
    private var id = 0
    private val registers: MutableMap<Node, Register> = HashMap<Node, Register>()

    override fun allocateRegisters(graph: IrGraph): MutableMap<Node, Register> {
        val visited: MutableSet<Node> = HashSet<Node>()
        visited.add(graph.endBlock())
        scan(graph.endBlock(), visited)
        return Map.copyOf<Node, Register>(this.registers)
    }

    private fun scan(node: Node, visited: MutableSet<Node>) {
        for (predecessor in node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited)
            }
        }
        if (needsRegister(node)) {
            this.registers.put(node, VirtualRegister(this.id++))
        }
    }

    companion object {
        private fun needsRegister(node: Node): Boolean {
            return !(node is ProjNode || node is StartNode || node is Block || node is ReturnNode)
        }
    }
}
