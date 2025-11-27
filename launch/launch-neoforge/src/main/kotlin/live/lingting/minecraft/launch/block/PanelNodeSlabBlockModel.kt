package live.lingting.minecraft.launch.block

import live.lingting.minecraft.PanelNodeSlabBlock
import live.lingting.minecraft.launch.model.NBlockModel
import live.lingting.minecraft.textures.Textures
import live.lingting.minecraft.world.IWorld

class PanelNodeSlabBlockModel : NBlockModel() {

    override val types: List<Class<out IWorld>> = listOf(PanelNodeSlabBlock::class.java)

    override fun register() {
        slab(Textures.BLOCK_PANEL_NODE_SLAB)
    }

}