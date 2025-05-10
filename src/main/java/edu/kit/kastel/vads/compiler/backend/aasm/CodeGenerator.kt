package edu.kit.kastel.vads.compiler.backend.aasm

import edu.kit.kastel.vads.compiler.backend.regalloc.Register
import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.node.*
import edu.kit.kastel.vads.compiler.ir.util.NodeSupport

class CodeGenerator {
    fun generateCode(program: MutableList<IrGraph>): String {
        val builder = StringBuilder()
        for (graph in program) {
            val allocator = AasmRegisterAllocator()
            val registers = allocator.allocateRegisters(graph)
            builder.append("function ")
                .append(graph.name())
                .append(" {\n")
            generateForGraph(graph, builder, registers)
            builder.append("}")
        }
        return builder.toString()
    }

    private fun generateForGraph(graph: IrGraph, builder: StringBuilder, registers: MutableMap<Node, Register>) {
        val visited: MutableSet<Node> = HashSet<Node>()
        scan(graph.endBlock(), visited, builder, registers)
    }

    private fun scan(
        node: Node,
        visited: MutableSet<Node>,
        builder: StringBuilder,
        registers: MutableMap<Node, Register>
    ) {
        for (predecessor in node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited, builder, registers)
            }
        }

        when (node) {
            is AddNode -> binary(builder, registers, node, "add")
            is SubNode -> binary(builder, registers, node, "sub")
            is MulNode -> binary(builder, registers, node, "mul")
            is DivNode -> binary(builder, registers, node, "div")
            is ModNode -> binary(builder, registers, node, "mod")
            is ReturnNode -> builder.repeat(" ", 2).append("ret ")
                .append(registers[NodeSupport.predecessorSkipProj(node, ReturnNode.Companion.RESULT)])

            is ConstIntNode -> builder.repeat(" ", 2)
                .append(registers[node])
                .append(" = const ")
                .append(node.value())

            is Phi -> throw UnsupportedOperationException("phi")
            is Block, is ProjNode, is StartNode -> {
                // do nothing, skip line break
                return
            }
        }
        builder.append("\n")
    }

    companion object {
        private fun binary(
            builder: StringBuilder,
            registers: MutableMap<Node, Register>,
            node: BinaryOperationNode,
            opcode: String
        ) {
            builder.repeat(" ", 2).append(registers.get(node))
                .append(" = ")
                .append(opcode)
                .append(" ")
                .append(registers.get(NodeSupport.predecessorSkipProj(node, BinaryOperationNode.Companion.LEFT)))
                .append(" ")
                .append(registers.get(NodeSupport.predecessorSkipProj(node, BinaryOperationNode.Companion.RIGHT)))
        }
    }
}
