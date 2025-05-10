package edu.kit.kastel.vads.compiler.ir.util

import edu.kit.kastel.vads.compiler.ir.util.DebugInfo.NoInfo

/** This is a dirty trick as we don't have Scoped Values.
 * It allows tracking debug info without having to pass it
 * down all the layers. */
object DebugInfoHelper {
    var debugInfo: DebugInfo = NoInfo.INSTANCE
}
