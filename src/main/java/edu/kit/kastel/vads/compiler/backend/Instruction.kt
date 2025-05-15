package edu.kit.kastel.vads.compiler.backend

enum class Name {
    MOVL,
    ADDL, SUBL,
    IMULL, IDIVL, // Integer = signed variant
    CLTD, // Convert Long To Double
    CLTQ, // Convert Long To Quad
}

sealed interface Operand
data class Immediate(val value: Int) : Operand {
    override fun toString(): String = "$$value"
}

data class Memory(val address: String) : Operand {
    override fun toString(): String = address
}

sealed interface Register : Operand

data class VirtualRegister(val id: Int) : Register {
    override fun toString() = "%t$id"
}

enum class RealRegister : Register {
    EAX, EBX, ECX, EDX,
    ESP, EBP,
    ESI, EDI
    ;

    override fun toString() = "%${name.lowercase()}"
}

class Instruction(
    val name: Name,
    vararg val operands: Operand
) {
    override fun toString(): String {
        val ops = operands.joinToString(", ") { it.toString() }
        return "${name.name.lowercase()} $ops"
    }
}
