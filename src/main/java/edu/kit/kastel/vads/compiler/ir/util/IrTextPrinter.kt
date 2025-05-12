package edu.kit.kastel.vads.compiler.ir.util

import edu.kit.kastel.vads.compiler.backend.aasm.Register
import edu.kit.kastel.vads.compiler.backend.aasm.VirtualRegister
import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.node.AddNode
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode
import edu.kit.kastel.vads.compiler.ir.node.Block
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode
import edu.kit.kastel.vads.compiler.ir.node.DivNode
import edu.kit.kastel.vads.compiler.ir.node.ModNode
import edu.kit.kastel.vads.compiler.ir.node.MulNode
import edu.kit.kastel.vads.compiler.ir.node.Node
import edu.kit.kastel.vads.compiler.ir.node.Phi
import edu.kit.kastel.vads.compiler.ir.node.ProjNode
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode
import edu.kit.kastel.vads.compiler.ir.node.StartNode
import edu.kit.kastel.vads.compiler.ir.node.SubNode


import kotlin.collections.MutableMap
import kotlin.collections.MutableSet

class AasmRegisterAllocator {
    private var id = 0
    private val registers: MutableMap<Node, Register> = mutableMapOf()

    fun allocateRegisters(graph: IrGraph): Map<Node, Register> {
        val visited: MutableSet<Node> = mutableSetOf()
        visited.add(graph.endBlock)
        scan(graph.endBlock, visited)
        return registers.toMap()
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

class IrTextPrinter {
    fun generateCode(program: List<IrGraph>): String = buildString {
        program.forEach { graph ->
            val allocator = AasmRegisterAllocator()
            val registers = allocator.allocateRegisters(graph)

            append("function ${graph.name} {\n")
            generateForGraph(graph, this, registers)
            append("}")
        }
    }

    private fun generateForGraph(
        graph: IrGraph,
        builder: StringBuilder,
        registers: Map<Node, Register>
    ) {
        val visited = mutableSetOf<Node>()
        scan(graph.endBlock, visited, builder, registers)
    }

    private fun scan(
        node: Node,
        visited: MutableSet<Node>,
        builder: StringBuilder,
        registers: Map<Node, Register>
    ) {
        node.predecessors()
            .filter { visited.add(it) }
            .forEach { scan(it, visited, builder, registers) }
        when (node) {
            is BinaryOperationNode -> {
                val opcode = when (node) {
                    is AddNode -> "add"
                    is SubNode -> "sub"
                    is MulNode -> "mul"
                    is DivNode -> "div"
                    is ModNode -> "mod"
                }
                val op = generateBinaryOperation(registers, node, opcode)
                builder.append("  $op")
            }

            is ReturnNode -> {
                val result = registers[NodeSupport.predecessorSkipProj(node, ReturnNode.Companion.RESULT)]
                builder.append("  ret $result")
            }

            is ConstIntNode -> {
                val register = registers[node]
                builder.append("  $register = const ${node.value}")
            }

            is Phi -> throw UnsupportedOperationException("phi")

            is Block, is ProjNode, is StartNode -> return
        }

        builder.append("\n")
    }

    private fun generateBinaryOperation(
        registers: Map<Node, Register>,
        node: BinaryOperationNode,
        opcode: String
    ): String {
        val targetRegister = registers[node]
        val leftRegister = registers[NodeSupport.predecessorSkipProj(node, BinaryOperationNode.Companion.LEFT)]
        val rightRegister = registers[NodeSupport.predecessorSkipProj(node, BinaryOperationNode.Companion.RIGHT)]

        return "$targetRegister = $opcode $leftRegister $rightRegister"
    }
}