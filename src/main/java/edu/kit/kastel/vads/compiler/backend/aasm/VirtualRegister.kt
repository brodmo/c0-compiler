package edu.kit.kastel.vads.compiler.backend.aasm

import edu.kit.kastel.vads.compiler.backend.regalloc.Register

data class VirtualRegister(val id: Int) : Register {
    override fun toString(): String {
        return "%" + this.id
    }
}
