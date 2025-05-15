package edu.kit.kastel.vads.compiler.backend

class RegisterAllocator {

    fun allocate(instructions: List<Instruction>): List<Instruction> {
        val registers = mapRegisters(instructions)
        // todo handle spilled registers
        return instructions.map { inst -> inst.copy(operands = inst.operands.map { registers[it] ?: it }) }
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
}
