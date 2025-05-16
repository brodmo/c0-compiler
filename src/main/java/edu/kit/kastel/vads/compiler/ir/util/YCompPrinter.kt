package edu.kit.kastel.vads.compiler.ir.util

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.node.*
import java.util.*

class YCompPrinter(private val graph: IrGraph) {

    private val clusters: MutableMap<Block, MutableSet<Node>> = mutableMapOf()
    private val ids: MutableMap<Node, Int> = mutableMapOf()
    private var nodeCounter = 0
    private var blockCounter = 0

    private fun prepare(node: Node, seen: MutableSet<Node>) {
        if (!seen.add(node)) {
            return
        }

        if (node !is Block) {
            clusters.computeIfAbsent(node.block!!) {
                Collections.newSetFromMap(mutableMapOf())
            }.add(node)
        }

        for (predecessor in node.predecessors) {
            prepare(predecessor, seen)
        }

        if (node == graph.endBlock) {
            clusters[graph.endBlock] = mutableSetOf()
        }
    }

    private fun dumpGraphAsString(): String {
        val result = StringBuilder()

        result.append("graph: {")
        val graphName = graph.name
        result.append("\n  title: ").append('"').append(graphName).append('"').append("\n")

        result.append(
            """
            display_edge_labels: yes
            layoutalgorithm: mindepth //$ "Compilergraph"
            manhattan_edges: yes
            port_sharing: no
            orientation: top_to_bottom
            """.trimMargin().replaceIndent("  ")
        )

        for (color in VcgColor.entries) {
            result.append("\n  colorentry ").append(color.id()).append(": ").append(color.rgb)
        }

        result.append("\n")

        result.append(formatMethod(graphName).replaceIndent("  "))

        result.append("}")

        return result.toString()
    }

    private fun formatMethod(name: String): String {
        val result = StringBuilder()

        result.append("graph: {")
        result.append("\n  title: ").append('"').append("method").append('"')
        result.append("\n  label: ").append('"').append(name).append('"')
        result.append("\n  color: ").append(VcgColor.ROOT_BLOCK.id())

        for ((block, nodes) in clusters) {
            result.append("\n").append(formatBlock(block, nodes).replaceIndent("  "))
        }

        result.append("}")

        return result.toString()
    }

    private fun formatBlock(block: Block, nodes: Set<Node>): String {
        val result = StringBuilder("graph: {")
        result.append("\n  title: ").append('"').append(nodeTitle(block)).append('"')
        result.append("\n  label: ").append('"').append(nodeLabel(block)).append('"')
        result.append("\n  status: clustered")
        result.append("\n  color: ").append(VcgColor.BLOCK.id())
        result.append("\n")

        for (node in nodes) {
            result.append(formatNode(node).replaceIndent("  "))
            result.append(formatInputEdges(node).replaceIndent("  "))
        }
        result.append(formatControlflowEdges(block))

        result.append(formatSchedule(block))

        result.append("\n}")

        return result.toString()
    }

    private fun formatNode(node: Node): String {
        val infoText = "I am an info text for $node"

        var result = "node: {"
        result += "\n  title: ${'"'}${nodeTitle(node)}${'"'}\n"
        result += "\n  label: ${'"'}${nodeLabel(node)}${'"'}\n"
        result += "\n  color: ${nodeColor(node).id()}"
        result += "\n  info1: ${'"'}$infoText${'"'}"
        result += "\n}"

        return result
    }

    private fun formatInputEdges(node: Node): String {
        val edges = node.predecessors.mapIndexed { index, predecessor ->
            Edge(
                predecessor,
                node,
                index,
                edgeColor(predecessor, node)
            )
        }
        return formatEdges(edges, "\n  priority: 50")
    }

    private fun edgeColor(src: Node, dst: Node): VcgColor? {
        if (nodeColor(src) != VcgColor.NORMAL) {
            return nodeColor(src)
        }
        if (nodeColor(dst) != VcgColor.NORMAL) {
            return nodeColor(dst)
        }
        return null
    }

    private fun formatControlflowEdges(block: Block): String {
        val result = StringJoiner("\n")
        val parents = block.predecessors
        for (parent in parents) {
            if (parent is ReturnNode) {
                // Return needs no label
                result.add(formatControlflowEdge(parent, block, ""))
            } else {
                throw RuntimeException("Unknown paren type: $parent")
            }
        }

        return result.toString()
    }

    private fun formatControlflowEdge(source: Node, dst: Block, label: String): String {
        var result = "edge: {"
        result += "\n  sourcename: ${'"'}${nodeTitle(source)}${'"'}"
        result += "\n  targetname: ${'"'}${nodeTitle(dst)}${'"'}"
        result += "\n  label: ${'"'}$label${'"'}"
        result += "\n  color: ${VcgColor.CONTROL_FLOW.id()}"
        result += "\n}"
        return result
    }

    private fun formatEdges(edges: Collection<Edge>, additionalProps: String): String {
        val result = StringJoiner("\n")
        for (edge in edges) {
            val inner = StringBuilder()
            // edge: {sourcename: "n74" targetname: "n71" label: "0" class:14 priority:50 color:blue}
            inner.append("edge: {")
            inner.append("\n  sourcename: ").append('"').append(nodeTitle(edge.src)).append('"')
            inner.append("\n  targetname: ").append('"').append(nodeTitle(edge.dst)).append('"')
            inner.append("\n  label: ").append('"').append(edge.index).append('"')
            edge.color?.let { color -> inner.append("\n  color: ").append(color.id()) }
            inner.append(additionalProps)
            inner.append("\n}")
            result.add(inner)
        }

        return result.toString()
    }

    private fun formatSchedule(block: Block): String {
        // Once you have a schedule, you might want to also emit it :)
        return formatEdges(emptyList(), "\n  color: ${VcgColor.SCHEDULE.id()}")
    }

    @Suppress("DuplicateBranchesInSwitch", "REDUNDANT_ELSE_IN_WHEN")
    private fun nodeColor(node: Node): VcgColor {
        return when (node) {
            is BinaryOperationNode -> VcgColor.NORMAL
            is Block -> VcgColor.NORMAL
            is ConstIntNode -> VcgColor.NORMAL
            is Phi -> VcgColor.PHI
            is ProjNode -> {
                if (node.projectionInfo == ProjectionInfo.SIDE_EFFECT) {
                    VcgColor.MEMORY
                } else if (node.projectionInfo == ProjectionInfo.RESULT) {
                    VcgColor.NORMAL
                } else {
                    VcgColor.NORMAL
                }
            }

            is ReturnNode -> VcgColor.CONTROL_FLOW
            is StartNode -> VcgColor.CONTROL_FLOW
            else -> VcgColor.NORMAL
        }
    }

    private fun nodeTitle(node: Node): String {
        if (node is Block) {
            return when {
                node == graph.startBlock -> "start-block"
                node == graph.endBlock -> "end-block"
                else -> "block-${idFor(node)}"
            }
        }
        return "node-${idFor(node)}"
    }

    private fun nodeLabel(node: Node): String {
        return when {
            node == graph.startBlock -> "start-block"
            node == graph.endBlock -> "end-block"
            else -> node.toString()
        }
    }

    private fun idFor(node: Node): Int {
        return if (node is Block) {
            ids.computeIfAbsent(node) { blockCounter++ }
        } else {
            ids.computeIfAbsent(node) { nodeCounter++ }
        }
    }

    private data class Edge(
        val src: Node,
        val dst: Node,
        val index: Int,
        val color: VcgColor?
    )

    private enum class VcgColor(val rgb: String) {
        // colorentry 100: 204 204 204  gray
        // colorentry 101: 222 239 234  faint green
        // colorentry 103: 242 242 242  white-ish
        // colorentry 104: 153 255 153  light green
        // colorentry 105: 153 153 255  blue
        // colorentry 106: 255 153 153  red
        // colorentry 107: 255 255 153  yellow
        // colorentry 108: 255 153 255  pink
        // colorentry 110: 127 127 127  dark gray
        // colorentry 111: 153 255 153  light green
        // colorentry 114: 153 153 255  blue
        CONTROL_FLOW("255 153 153"),
        MEMORY("153 153 255"),
        NORMAL("242 242 242"),
        SPECIAL("255 153 255"),
        CONST("255 255 153"),
        PHI("153 255 153"),
        ROOT_BLOCK("204 204 204"),
        BLOCK("222 239 234"),
        SCHEDULE("255 153 255");

        fun id(): Int = 100 + ordinal
    }

    companion object {
        fun print(graph: IrGraph): String {
            val printer = YCompPrinter(graph)
            printer.prepare(graph.endBlock, mutableSetOf())
            return printer.dumpGraphAsString()
        }
    }
}