package edu.kit.kastel.vads.compiler.backend.aasm

import edu.kit.kastel.vads.compiler.backend.regalloc.Register
import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.node.*
import edu.kit.kastel.vads.compiler.ir.util.NodeSupport

class CodeGenerator {
    fun generateCode(program: List<IrGraph>): String = buildString {
        program.forEach { graph ->
            val allocator = AasmRegisterAllocator()
            val registers = allocator.allocateRegisters(graph)

            append("function ${graph.name()} {\n")
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
        scan(graph.endBlock(), visited, builder, registers)
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
                val result = registers[NodeSupport.predecessorSkipProj(node, ReturnNode.RESULT)]
                builder.append("  ret $result")
            }

            is ConstIntNode -> {
                val register = registers[node]
                builder.append("  $register = const ${node.value()}")
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
        val leftRegister = registers[NodeSupport.predecessorSkipProj(node, BinaryOperationNode.LEFT)]
        val rightRegister = registers[NodeSupport.predecessorSkipProj(node, BinaryOperationNode.RIGHT)]

        return "$targetRegister = $opcode $leftRegister $rightRegister"
    }
}