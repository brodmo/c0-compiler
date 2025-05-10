package edu.kit.kastel.vads.compiler.ir.util

import edu.kit.kastel.vads.compiler.Span

/** Provides information to ease debugging */
interface DebugInfo {
    enum class NoInfo : DebugInfo {
        INSTANCE
    }

    @JvmRecord
    data class SourceInfo(val span: Span) : DebugInfo
}
