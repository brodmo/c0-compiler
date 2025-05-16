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
                    Instruction(Name.MOVL, src, TEMP_REG),
                    Instruction(inst.name, TEMP_REG, dst),
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
        Instruction(Name.PUSHQ, PointerRegisters.RBP),
        Instruction(Name.MOVQ, PointerRegisters.RSP, PointerRegisters.RBP),
        Instruction(Name.SUBQ, Immediate(stackSize), PointerRegisters.RSP),
    )
    return prolog + instructions
}