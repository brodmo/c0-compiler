package edu.kit.kastel.vads.compiler.backend.aasm

enum class Mnemonic {
    MOVL,
    ADDL, SUBL,
    IMULL, IDIVL, // Integer = signed variant
    CLTD, // Convert Long To Double
}

sealed interface Operand
data class Immediate(val value: Int) : Operand
data class Memory(val address: String) : Operand

enum class Register : Operand {
    EAX, EBX, ECX, EDX,
    ESP, EBP,
    ESI, EDI
}

class Instruction(
    val mnemonic: Mnemonic,
    vararg val operands: Operand
) {
    override fun toString(): String {
        val ops = operands.joinToString(", ") { formatOperand(it) }
        return "${mnemonic.name} $ops"
    }

    private fun formatOperand(op: Operand) = when (op) {
        is Register -> "%${op.name.lowercase()}"
        is Immediate -> "$${op.value}"
        is Memory -> op.address
    }
}
