package live.lingting.minecraft.launch.block

import live.lingting.minecraft.PanelNodeStairBlock
import live.lingting.minecraft.launch.model.NBlockModel
import live.lingting.minecraft.textures.Textures
import live.lingting.minecraft.world.IWorld

/**
 * @author lingting 2025/11/27 17:04
 */
class PanelNodeStairsBlockModel : NBlockModel() {

    override val types: List<Class<out IWorld>> = listOf(PanelNodeStairBlock::class.java)

    override fun register() {
        stair(Textures.BLOCK_PANEL_NODE_STAIR)
    }
}