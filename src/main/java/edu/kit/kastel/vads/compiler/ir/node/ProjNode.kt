package edu.kit.kastel.vads.compiler.ir.node

class ProjNode(block: Block, `in`: Node, private val projectionInfo: ProjectionInfo) : Node(block, `in`) {
    override fun info(): String {
        return this.projectionInfo.toString()
    }

    fun projectionInfo(): ProjectionInfo {
        return projectionInfo
    }

    interface ProjectionInfo

    enum class SimpleProjectionInfo : ProjectionInfo {
        RESULT, SIDE_EFFECT
    }

    companion object {
        const val IN: Int = 0
    }
}
