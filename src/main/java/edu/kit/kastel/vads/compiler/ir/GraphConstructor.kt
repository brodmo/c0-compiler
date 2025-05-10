package edu.kit.kastel.vads.compiler.ir

import edu.kit.kastel.vads.compiler.ir.node.*
import edu.kit.kastel.vads.compiler.ir.node.ProjNode.SimpleProjectionInfo
import edu.kit.kastel.vads.compiler.ir.optimize.Optimizer
import edu.kit.kastel.vads.compiler.parser.symbol.Name
import java.util.Map
import java.util.function.Function
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet

internal class GraphConstructor(private val optimizer: Optimizer, name: String) {
    private val graph: IrGraph
    private val currentDef: MutableMap<Name, MutableMap<Block, Node>> = HashMap<Name, MutableMap<Block, Node>>()
    private val incompletePhis: MutableMap<Block, MutableMap<Name, Phi>> = HashMap<Block, MutableMap<Name, Phi>>()
    private val currentSideEffect: MutableMap<Block, Node> = HashMap<Block, Node>()
    private val incompleteSideEffectPhis: MutableMap<Block, Phi> = HashMap<Block, Phi>()
    private val sealedBlocks: MutableSet<Block> = HashSet<Block>()
    private val currentBlock: Block

    init {
        this.graph = IrGraph(name)
        this.currentBlock = this.graph.startBlock()
        // the start block never gets any more predecessors
        sealBlock(this.currentBlock)
    }

    fun newStart(): Node {
        assert(currentBlock() == this.graph.startBlock()) { "start must be in start block" }
        return StartNode(currentBlock())
    }

    fun newAdd(left: Node, right: Node): Node {
        return this.optimizer.transform(AddNode(currentBlock(), left, right))
    }

    fun newSub(left: Node, right: Node): Node {
        return this.optimizer.transform(SubNode(currentBlock(), left, right))
    }

    fun newMul(left: Node, right: Node): Node {
        return this.optimizer.transform(MulNode(currentBlock(), left, right))
    }

    fun newDiv(left: Node, right: Node): Node {
        return this.optimizer.transform(DivNode(currentBlock(), left, right, readCurrentSideEffect()))
    }

    fun newMod(left: Node, right: Node): Node {
        return this.optimizer.transform(ModNode(currentBlock(), left, right, readCurrentSideEffect()))
    }

    fun newReturn(result: Node): Node {
        return ReturnNode(currentBlock(), readCurrentSideEffect(), result)
    }

    fun newConstInt(value: Int): Node {
        // always move const into start block, this allows better deduplication
        // and resultingly in better value numbering
        return this.optimizer.transform(ConstIntNode(this.graph.startBlock(), value))
    }

    fun newSideEffectProj(node: Node): Node {
        return ProjNode(currentBlock(), node, SimpleProjectionInfo.SIDE_EFFECT)
    }

    fun newResultProj(node: Node): Node {
        return ProjNode(currentBlock(), node, SimpleProjectionInfo.RESULT)
    }

    fun currentBlock(): Block {
        return this.currentBlock
    }

    fun newPhi(): Phi {
        // don't transform phi directly, it is not ready yet
        return Phi(currentBlock())
    }

    fun graph(): IrGraph {
        return this.graph
    }

    fun writeVariable(variable: Name, block: Block, value: Node) {
        this.currentDef.computeIfAbsent(variable, Function { `_`: Name -> HashMap() }).put(block, value)
    }

    fun readVariable(variable: Name, block: Block): Node {
        val node = this.currentDef.getOrDefault(variable, Map.of<Block, Node>()).get(block)
        if (node != null) {
            return node
        }
        return readVariableRecursive(variable, block)
    }


    private fun readVariableRecursive(variable: Name, block: Block): Node {
        var `val`: Node
        if (!this.sealedBlocks.contains(block)) {
            `val` = newPhi()
            this.incompletePhis.computeIfAbsent(block, Function { `_`: Block -> HashMap() })
                .put(variable, `val`)
        } else if (block.predecessors().size == 1) {
            `val` = readVariable(variable, block.predecessors().first().block())
        } else {
            `val` = newPhi()
            writeVariable(variable, block, `val`)
            `val` = addPhiOperands(variable, `val`)
        }
        writeVariable(variable, block, `val`)
        return `val`
    }

    fun addPhiOperands(variable: Name, phi: Phi): Node {
        for (pred in phi.block().predecessors()) {
            phi.appendOperand(readVariable(variable, pred.block()))
        }
        return tryRemoveTrivialPhi(phi)
    }

    fun tryRemoveTrivialPhi(phi: Phi): Node {
        // TODO: the paper shows how to remove trivial phis.
        // as this is not a problem in Lab 1 and it is just
        // a simplification, we recommend to implement this
        // part yourself.
        return phi
    }

    fun sealBlock(block: Block) {
        for (entry in this.incompletePhis.getOrDefault(block, Map.of<Name, Phi>()).entries) {
            addPhiOperands(entry.key, entry.value)
        }
        this.sealedBlocks.add(block)
    }

    fun writeCurrentSideEffect(node: Node) {
        writeSideEffect(currentBlock(), node)
    }

    private fun writeSideEffect(block: Block, node: Node) {
        this.currentSideEffect.put(block, node)
    }

    fun readCurrentSideEffect(): Node {
        return readSideEffect(currentBlock())
    }

    private fun readSideEffect(block: Block): Node {
        val node = this.currentSideEffect.get(block)
        if (node != null) {
            return node
        }
        return readSideEffectRecursive(block)
    }

    private fun readSideEffectRecursive(block: Block): Node {
        var `val`: Node
        if (!this.sealedBlocks.contains(block)) {
            `val` = newPhi()
            val old = this.incompleteSideEffectPhis.put(block, `val`)
            assert(old == null) { "double readSideEffectRecursive for " + block }
        } else if (block.predecessors().size == 1) {
            `val` = readSideEffect(block.predecessors().first().block())
        } else {
            `val` = newPhi()
            writeSideEffect(block, `val`)
            `val` = addPhiOperands(`val`)
        }
        writeSideEffect(block, `val`)
        return `val`
    }

    fun addPhiOperands(phi: Phi): Node {
        for (pred in phi.block().predecessors()) {
            phi.appendOperand(readSideEffect(pred.block()))
        }
        return tryRemoveTrivialPhi(phi)
    }
}
