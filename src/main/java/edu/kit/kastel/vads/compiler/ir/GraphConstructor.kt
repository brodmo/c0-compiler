package edu.kit.kastel.vads.compiler.ir

import edu.kit.kastel.vads.compiler.ir.node.*
import edu.kit.kastel.vads.compiler.ir.optimize.Optimizer
import edu.kit.kastel.vads.compiler.parser.symbol.Name

internal class GraphConstructor(private val optimizer: Optimizer, name: String) {
    val graph: IrGraph = IrGraph(name)
    private val currentDef = mutableMapOf<Name, MutableMap<Block, Node>>()
    private val incompletePhis = mutableMapOf<Block, MutableMap<Name, Phi>>()
    private val currentSideEffect = mutableMapOf<Block, Node>()
    private val incompleteSideEffectPhis = mutableMapOf<Block, Phi>()
    private val sealedBlocks = mutableSetOf<Block>()
    val currentBlock: Block = graph.startBlock

    init {
        // the start block never gets any more predecessors
        sealBlock(currentBlock)
    }

    fun newStart(): Node {
        require(currentBlock == graph.startBlock) { "start must be in start block" }
        return StartNode(currentBlock)
    }

    fun newBinaryOperation(operator: BinaryOperator, left: Node, right: Node): Node {
        val sideEffect = if (operator.hasSideEffect) readCurrentSideEffect() else null
        val node = BinaryOperationNode(currentBlock, operator, left, right, sideEffect)
        return optimizer.transform(node)
    }

    fun newReturn(result: Node): Node =
        ReturnNode(currentBlock, readCurrentSideEffect(), result)

    fun newConstInt(value: Int): Node =
        // always move const into start block, this allows better deduplication
        // and resultingly in better value numbering
        optimizer.transform(ConstIntNode(graph.startBlock, value))

    fun newSideEffectProj(node: Node): Node =
        ProjNode(currentBlock, node, ProjectionInfo.SIDE_EFFECT)

    fun newResultProj(node: Node): Node =
        ProjNode(currentBlock, node, ProjectionInfo.RESULT)

    fun writeVariable(variable: Name, block: Block, value: Node) {
        currentDef.getOrPut(variable) { mutableMapOf() }[block] = value
    }

    fun readVariable(variable: Name, block: Block): Node =
        currentDef[variable]?.get(block) ?: readVariableRecursive(variable, block)

    private fun readVariableRecursive(variable: Name, block: Block): Node {
        val value = when {
            block !in sealedBlocks -> {
                Phi(block).also {
                    incompletePhis.getOrPut(block) { mutableMapOf() }[variable] = it
                }
            }

            block.predecessors.size == 1 -> {
                readVariable(variable, block.predecessors.first().block!!)
            }

            else -> {
                Phi(block).let {
                    writeVariable(variable, block, it)
                    addPhiOperands(variable, it)
                }
            }
        }

        writeVariable(variable, block, value)
        return value
    }

    private fun readSideEffectRecursive(block: Block): Node {
        val value = when {
            block !in sealedBlocks -> {
                Phi(block).also {
                    val old = incompleteSideEffectPhis.put(block, it)
                    require(old == null) { "double readSideEffectRecursive for $block" }
                }
            }

            block.predecessors.size == 1 -> {
                readSideEffect(block.predecessors.first().block!!)
            }

            else -> {
                Phi(block).let {
                    writeSideEffect(block, it)
                    addPhiOperands(it)
                }
            }
        }

        writeSideEffect(block, value)
        return value
    }

    fun addPhiOperands(variable: Name, phi: Phi): Node {
        phi.block?.predecessors?.forEach { pred ->
            phi.addPredecessor(readVariable(variable, pred.block!!))
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
        incompletePhis[block]?.forEach { (key, value) ->
            addPhiOperands(key, value)
        }
        incompleteSideEffectPhis[block]?.let { phi ->
            addPhiOperands(phi)
        }
        sealedBlocks.add(block)
    }

    fun writeCurrentSideEffect(node: Node) {
        writeSideEffect(currentBlock, node)
    }

    private fun writeSideEffect(block: Block, node: Node) {
        currentSideEffect[block] = node
    }

    fun readCurrentSideEffect(): Node = readSideEffect(currentBlock)

    private fun readSideEffect(block: Block): Node =
        currentSideEffect[block] ?: readSideEffectRecursive(block)

    fun addPhiOperands(phi: Phi): Node {
        phi.block?.predecessors?.forEach { pred ->
            phi.addPredecessor(readSideEffect(pred.block!!))
        }
        return tryRemoveTrivialPhi(phi)
    }
}
