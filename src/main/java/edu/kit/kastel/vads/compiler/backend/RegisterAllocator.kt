package edu.kit.kastel.vads.compiler.backend

val TEMP_REG = GeneralRegister.ECX

class RegisterAllocator {

    fun allocate(instructions: List<Instruction>): List<Instruction> {
        val registers = mapRegisters(instructions)
        return finalizeAssembly(instructions.map { inst ->
            inst.copy(operands = inst.operands.map { registers[it] ?: it })
        })
    }

    private fun mapRegisters(instructions: List<Instruction>): Map<VirtualRegister, RealRegister> {
        // TODO add graph coloring
        val registers = produceRegisters()
        return instructions
            .flatMap { it.operands.toList() }
            .toSet()
            .filterIsInstance<VirtualRegister>()
            .associateWith { registers.next() }
    }

    private fun produceRegisters(): Iterator<RealRegister> = sequence {
        GeneralRegister.entries
            .filter { it !in listOf(GeneralRegister.EAX, GeneralRegister.EDX, TEMP_REG) }
            .forEach { yield(it) }
        generateSequence(-4) { it - 4 }
            .forEach { yield(SpilledRegister(it)) }
    }.iterator()

}
