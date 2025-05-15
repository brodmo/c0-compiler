package edu.kit.kastel.vads.compiler.backend

import edu.kit.kastel.vads.compiler.ir.node.*

typealias UpDown = Pair<Operand?, List<Instruction>>

class InstructionSelector : NodeVisitor<UpDown> {
    val knownUps = mutableMapOf<Node, Operand?>()
    var registerId = 0

    private fun rememberUps(node: Node, function: (Node) -> UpDown): UpDown {
        knownUps[node]?.let { return it to emptyList() }
        val (up, down) = function(node)
        knownUps[node] = up
        return up to down
    }

    override fun visit(node: BinaryOperationNode): UpDown = rememberUps(node) {
        val (leftUp, leftDown) = node.left.accept(this)
        val (rightUp, rightDown) = node.right.accept(this)
        val (_, sideEffectDown) = node.sideEffect?.accept(this) ?: (null to emptyList())
        val up = VirtualRegister(registerId++)
        val name = when (node.operator) {
            BinaryOperator.ADD -> Name.ADDL
            BinaryOperator.SUBTRACT -> Name.SUBL
            BinaryOperator.MULTIPLY -> Name.IMULL
            BinaryOperator.DIVIDE, BinaryOperator.MODULO -> Name.IDIVL
        }
        val opDown = when (name) {
            Name.IDIVL -> {
                val resultRegister = if (node.operator == BinaryOperator.DIVIDE) RealRegister.EAX else RealRegister.EDX
                listOf(
                    Instruction(Name.MOVL, leftUp!!, RealRegister.EAX),
                    Instruction(Name.CLTD),
                    Instruction(Name.IDIVL, rightUp!!),
                    Instruction(Name.MOVL, resultRegister, up)
                )
            }

            else -> listOf(
                Instruction(Name.MOVL, leftUp!!, up),
                Instruction(name, rightUp!!, up),
            )
        }
        up to leftDown + rightDown + sideEffectDown + opDown
    }

    override fun visit(node: ConstIntNode): UpDown = rememberUps(node) { Immediate(node.value) to emptyList() }

    override fun visit(node: StartNode): UpDown = null to emptyList()

    override fun visit(node: ReturnNode): UpDown = rememberUps(node) {
        val (resultUp, resultDown) = node.result.accept(this)
        val (_, sideEffectDown) = node.sideEffect.accept(this)
        val down = sideEffectDown + resultDown + listOf(
            Instruction(Name.MOVL, resultUp!!, RealRegister.EAX)
        )
        RealRegister.EAX to down
    }

    override fun visit(node: ProjNode): UpDown = rememberUps(node) { node.pred.accept(this) }

    override fun visit(node: Block): UpDown = rememberUps(node) {
        null to node.predecessors.flatMap { pred ->
            val (_, down) = pred.accept(this)
            down
        }
    }

    // This is what the phi is for right, deciding which predecessor value to pass on
    // Assume we only have one for now
    override fun visit(node: Phi): UpDown = rememberUps(node) { node.predecessors[0].accept(this) }
}
