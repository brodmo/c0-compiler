package edu.kit.kastel.vads.compiler.backend

fun finalizeAssembly(instructions: List<Instruction>): List<Instruction> {
    return addProlog(eliminateMemToMemInstructions(instructions))
}

private fun eliminateMemToMemInstructions(instructions: List<Instruction>): List<Instruction> {
    return instructions.flatMap { inst ->
        when {
            inst.operands.filterIsInstance<SpilledRegister>().size == 2 -> {
                val (src, dst) = inst.operands
                listOf(
                    Instruction(Name.MOV, TEMP_REG, src),
                    Instruction(inst.name, dst, TEMP_REG),
                )
            }

            else -> listOf(inst)
        }
    }
}

private fun addProlog(instructions: List<Instruction>): List<Instruction> {
    val stackSize = instructions
        .flatMap { it.operands.toList() }
        .filterIsInstance<SpilledRegister>()
        .maxOfOrNull { -it.offset + 4 } ?: 0
    val prolog = listOf(
        Instruction(Name.PUSH, PointerRegister.RBP),
        Instruction(Name.MOV, PointerRegister.RBP, PointerRegister.RSP),
        Instruction(Name.SUB, PointerRegister.RSP, Immediate(stackSize)),
    )
    return prolog + instructions
}