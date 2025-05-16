package edu.kit.kastel.vads.compiler.backend

val TEMP_REG = GeneralRegisters.ECX

class RegisterAllocator {

    fun allocate(instructions: List<Instruction>): List<Instruction> {
        val registers = mapRegisters(instructions)
        val result = instructions.map { inst -> inst.copy(operands = inst.operands.map { registers[it] ?: it }) }
        return eliminateMemToMemInstructions(result)
    }

    private fun mapRegisters(instructions: List<Instruction>): Map<VirtualRegister, RealRegister> {
        val registers = produceRegisters()
        return instructions
            .flatMap { it.operands.toList() }
            .toSet()
            .filterIsInstance<VirtualRegister>()
            .associateWith { registers.next() }
    }

    private fun produceRegisters(): Iterator<RealRegister> = sequence {
        GeneralRegisters.entries
            .filter { it !in listOf(GeneralRegisters.EAX, GeneralRegisters.EDX) }
            .forEach { yield(it) }
        generateSequence(0) { it + 4 }
            .forEach { yield(SpilledRegister(it)) }
    }.iterator()

    private fun eliminateMemToMemInstructions(instructions: List<Instruction>): List<Instruction> {
        return instructions.flatMap { inst -> when {
            inst.operands.filterIsInstance<SpilledRegister>().size == 2 -> {
                val (src, dst) = inst.operands
                listOf(
                    Instruction(Name.MOVL, src, TEMP_REG),
                    Instruction(inst.name, TEMP_REG, dst),
                )
            }
            else -> listOf(inst)
        } }
    }
}
