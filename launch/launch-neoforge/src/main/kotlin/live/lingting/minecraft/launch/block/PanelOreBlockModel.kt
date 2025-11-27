package live.lingting.minecraft.launch.block

import live.lingting.minecraft.PanelOreBlock
import live.lingting.minecraft.launch.model.NBlockModel
import live.lingting.minecraft.textures.Textures
import live.lingting.minecraft.world.IWorld

/**
 * @author lingting 2025/11/23 0:48
 */
class PanelOreBlockModel : NBlockModel() {

    override val types: List<Class<out IWorld>> = listOf(PanelOreBlock::class.java)

    override fun register() {
        val model = cubeAll(Textures.BLOCK_PANEL_ORE)
        forAllStates(model)
        simpleItem(model)
    }

}