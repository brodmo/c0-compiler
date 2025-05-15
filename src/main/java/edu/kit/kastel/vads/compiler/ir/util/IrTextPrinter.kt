package edu.kit.kastel.vads.compiler.ir.util


import edu.kit.kastel.vads.compiler.backend.Register
import edu.kit.kastel.vads.compiler.backend.VirtualRegister
import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.node.*

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
        for (predecessor in node.predecessors) {
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
        node.predecessors
            .filter { visited.add(it) }
            .forEach { scan(it, visited, builder, registers) }
        when (node) {
            is BinaryOperationNode -> {
                val op = generateBinaryOperation(registers, node)
                builder.append("  $op")
            }

            is ReturnNode -> {
                val result = registers[node.result.skipProj()]
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
    ): String {
        val targetRegister = registers[node]
        val leftRegister = registers[node.left.skipProj()]
        val rightRegister = registers[node.right.skipProj()]

        return "$targetRegister = ${node.operator.name.lowercase().substring(0, 3)} $leftRegister $rightRegister"
    }
}