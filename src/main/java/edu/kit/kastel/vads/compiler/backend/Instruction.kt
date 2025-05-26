package edu.kit.kastel.vads.compiler.backend

import kotlin.math.absoluteValue

enum class Name {
    MOV,
    RET,
    ADD, SUB,
    PUSH, POP,
    IMUL, IDIV, // Integer = signed variant
    CDQ, // Convert Double to Quad
}

sealed interface Operand {
    fun emit(): String
}

data class Immediate(val value: Int) : Operand {
    override fun emit(): String = "$value"
}


sealed interface Register : Operand

data class VirtualRegister(val id: Int) : Register {
    override fun emit() = "t$id"
}

sealed interface RealRegister : Register

sealed interface HardwareRegister : RealRegister

enum class GeneralRegister : HardwareRegister {
    EAX, EBX, ECX, EDX,
    ESI, EDI,
    R8D, R9D, R10D, R11D, R12D, R13D, R14D, R15D,
    ;

    override fun emit() = name.lowercase()
}

enum class PointerRegister : HardwareRegister {
    RBP, RSP;

    override fun emit() = name.lowercase()
}


data class SpilledRegister(val offset: Int) : RealRegister {
    val inside = listOf(
        PointerRegister.RBP.emit(),
        if (offset >= 0) "+" else "-",
        offset.absoluteValue
    ).joinToString(" ")
    override fun emit() = "[$inside]"
}


data class Instruction(
    val name: Name,
    val operands: List<Operand>
) {
    constructor(name: Name, vararg operands: Operand) : this(name, operands.toList())

    fun emit(): String {
        return listOfNotNull(
            name.name.lowercase(),
            "DWORD PTR".takeIf { operands.isNotEmpty() && operands.none { it is HardwareRegister } },
            operands.joinToString(", ") { it.emit() }
        ).joinToString(" ")
    }
}
