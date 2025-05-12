package edu.kit.kastel.vads.compiler.ir.util

import edu.kit.kastel.vads.compiler.ir.IrGraph
import edu.kit.kastel.vads.compiler.ir.node.Block
import edu.kit.kastel.vads.compiler.ir.node.Node
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Function

/** Outputs a DOT format string to visualize an [IrGraph]. */
class GraphVizPrinter(private val graph: IrGraph) {
    private val clusters: MutableMap<Block, MutableSet<Node>> = mutableMapOf()
    private val edges: MutableList<Edge> = mutableListOf()
    private val ids: MutableMap<Node, Int> = mutableMapOf()
    private val builder = StringBuilder()
    private var counter = 0

    private fun prepare(node: Node, seen: MutableSet<Node>) {
        if (!seen.add(node)) {
            return
        }

        if (node !is Block) {
            this.clusters.computeIfAbsent(node.safeBlock, Function { `_`: Block ->
                Collections.newSetFromMap<Node>(
                    mutableMapOf()
                )
            })
                .add(node)
        }
        var idx = 0
        for (predecessor in node.predecessors()) {
            this.edges.add(Edge(predecessor, node, idx++))
            prepare(predecessor, seen)
        }
        if (node === this.graph.endBlock) {
            this.clusters.put(this.graph.endBlock, mutableSetOf())
        }
    }

    private fun print() {
        this.builder.append("digraph \"")
            .append(this.graph.name)
            .append("\"")
            .append(
                """
                 {
                    compound=true;
                    layout=dot;
                    node [shape=box];
                    splines=ortho;
                    overlap=false;
                
                
                
                """.trimIndent()
            )

        this.clusters.forEach(BiConsumer { block: Block, nodes: MutableSet<Node> ->
            this.builder.append("    subgraph cluster_")
                .append(idFor(block))
                .append(" {\n")
                .repeat(" ", 8)
                .append("c_").append(idFor(block))
                .append(" [width=0, height=0, fixedsize=true, style=invis];\n")
            if (block == this.graph.endBlock) {
                this.builder.repeat(" ", 8)
                    .append("label=End;\n")
            }
            for (node in nodes) {
                this.builder.repeat(" ", 8)
                    .append(idFor(node))
                    .append(" [label=\"")
                    .append(labelFor(node))
                    .append("\"")
                val debugInfo = node.debugInfo
                if (debugInfo is DebugInfo.SourceInfo) {
                    this.builder.append(", tooltip=\"")
                        .append("source span: ")
                        .append(debugInfo.span)
                        .append("\"")
                }
                this.builder.append("];\n")
            }
            this.builder.append("    }\n\n")
        })

        for (edge in this.edges) {
            this.builder.repeat(" ", 4)
                .append(nameFor(edge.from))
                .append(" -> ")
                .append(nameFor(edge.to))
                .append(" [")
                .append("label=")
                .append(edge.idx)

            if (edge.from is Block) {
                this.builder.append(", ")
                    .append("ltail=")
                    .append("cluster_")
                    .append(idFor(edge.from))
            }
            if (edge.to is Block) {
                this.builder.append(", ")
                    .append("lhead=")
                    .append("cluster_")
                    .append(idFor(edge.to))
            }

            this.builder.append("];\n")
        }

        this.builder.append("}")
    }

    private fun idFor(node: Node): Int {
        return this.ids.computeIfAbsent(node, Function { `_`: Node -> this.counter++ })
    }

    private fun nameFor(node: Node): String {
        if (node is Block) {
            return "c_${idFor(node)}"
        }
        return idFor(node).toString()
    }

    private fun labelFor(node: Node): String {
        return node.toString()
    }

    internal data class Edge(val from: Node, val to: Node, val idx: Int)
    companion object {
        fun print(graph: IrGraph): String {
            val printer = GraphVizPrinter(graph)
            printer.prepare(graph.endBlock, mutableSetOf())
            printer.print()
            return printer.builder.toString()
        }
    }
}
