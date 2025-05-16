package edu.kit.kastel.vads.compiler.backend

enum class Name {
    MOVL,
    RET,
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
    RBP, RSP;

    override fun emit() = "%${name.lowercase()}"
}


data class SpilledRegister(val offset: Int) : RealRegister {
    override fun emit() = "$offset(${PointerRegisters.RBP.emit()})"
}


data class Instruction(
    val name: Name,
    val operands: List<Operand>
) {
    constructor(name: Name, vararg operands: Operand) : this(name, operands.toList())
    fun emit(): String {
        val ops = operands.joinToString(", ") { it.emit() }
        return "${name.name.lowercase()} $ops"
    }
}
