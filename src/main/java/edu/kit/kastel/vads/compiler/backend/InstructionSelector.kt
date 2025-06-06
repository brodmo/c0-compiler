package edu.kit.kastel.vads.compiler.backend

import edu.kit.kastel.vads.compiler.ir.node.*

typealias UpDown = Pair<Operand?, List<Instruction>>

class InstructionSelector : NodeVisitor<UpDown> {
    val knownUps = mutableMapOf<Node, Operand?>()
    val registerProducer = generateSequence(1) { it + 1 }.map { VirtualRegister(it) }.iterator()

    private fun rememberUps(node: Node, function: () -> UpDown): UpDown {
        knownUps[node]?.let { return it to emptyList() }
        val (up, down) = function()
        knownUps[node] = up
        return up to down
    }

    override fun visit(node: BinaryOperationNode): UpDown = rememberUps(node) {
        val (leftUp, leftDown) = node.left.accept(this)
        val (rightUp, rightDown) = node.right.accept(this)
        val (_, sideEffectDown) = node.sideEffect?.accept(this) ?: (null to emptyList())
        val up = registerProducer.next()
        val name = when (node.operator) {
            BinaryOperator.ADD -> Name.ADD
            BinaryOperator.SUBTRACT -> Name.SUB
            BinaryOperator.MULTIPLY -> Name.IMUL
            BinaryOperator.DIVIDE, BinaryOperator.MODULO -> Name.IDIV
        }
        val opDown = when (name) {
            Name.IMUL, Name.IDIV -> {
                val resultRegister =
                    if (node.operator == BinaryOperator.MODULO) GeneralRegister.EDX else GeneralRegister.EAX
                val temp = registerProducer.next()
                listOf(
                    Instruction(Name.MOV, GeneralRegister.EAX, leftUp!!),
                    Instruction(Name.CDQ),
                    Instruction(Name.MOV, temp, rightUp!!),
                    Instruction(name, temp),
                    Instruction(Name.MOV, up, resultRegister)
                )
            }

            else -> listOf(
                Instruction(Name.MOV, up, leftUp!!),
                Instruction(name, up, rightUp!!),
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
            Instruction(Name.MOV, GeneralRegister.EAX, resultUp!!),
            Instruction(Name.MOV, PointerRegister.RSP, PointerRegister.RBP),
            Instruction(Name.POP, PointerRegister.RBP),
            Instruction(Name.RET)
        )
        GeneralRegister.EAX to down
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
