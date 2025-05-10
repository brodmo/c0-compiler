package edu.kit.kastel.vads.compiler.ir.optimize

import edu.kit.kastel.vads.compiler.ir.node.Node
import java.util.function.Function
import kotlin.collections.HashMap
import kotlin.collections.MutableMap

/** This depends on [Node#equals(java.lang.Object)] and  [Node#hashCode()] methods.
 * As long as they take the block into account, it is only local, but replacement
 * is extremely simple.
 * When using classes like [HashMap] or [java.util.HashSet] without this optimization,
 * the [Node#equals(java.lang.Object)] and  [Node#hashCode()] methods must be adjusted. */
class LocalValueNumbering : Optimizer {
    private val knownNodes: MutableMap<Node, Node> = HashMap<Node, Node>()

    override fun transform(node: Node): Node {
        return this.knownNodes.computeIfAbsent(node, Function { n: Node -> n })
    }
}
