package edu.kit.kastel.vads.compiler.ir.node

class ProjNode(block: Block, `in`: Node, val projectionInfo: ProjectionInfo) : Node(block, `in`) {
    override fun info(): String {
        return this.projectionInfo.toString()
    }

    interface ProjectionInfo

    enum class SimpleProjectionInfo : ProjectionInfo {
        RESULT, SIDE_EFFECT
    }

    companion object {
        const val IN: Int = 0
    }
}
