package edu.kit.kastel.vads.compiler.backend

enum class Name {
    MOVL,
    ADDL, SUBL,
    IMULL, IDIVL, // Integer = signed variant
    CLTD, // Convert Long To Double
    CLTQ, // Convert Long To Quad
}

sealed interface Operand {
    fun emit(): String
}

data class Immediate(val value: Int) : Operand {
    override fun emit(): String = "$$value"
}

data class Memory(val address: String) : Operand {
    override fun emit(): String = address
}

sealed interface Register : Operand

data class VirtualRegister(val id: Int) : Register {
    override fun emit() = "%t$id"
}

sealed interface RealRegister : Register

enum class GeneralRegisters : RealRegister {
    EAX, EBX, ECX, EDX,
    ESI, EDI,
    R8D, R9D, R10D, R11D, R12D, R13D, R14D, R15D,
    ;

    override fun emit() = "%${name.lowercase()}"
}

enum class PointerRegisters : RealRegister {
    EBP, ESP;

    override fun emit() = "%${name.lowercase()}"
}


class Instruction(
    val name: Name,
    vararg val operands: Operand
) {
    fun emit(): String {
        val ops = operands.joinToString(", ") { it.emit() }
        return "${name.name.lowercase()} $ops"
    }
}
