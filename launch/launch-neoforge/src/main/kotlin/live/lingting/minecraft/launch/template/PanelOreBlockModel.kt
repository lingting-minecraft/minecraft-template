package live.lingting.minecraft.launch.template

import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.launch.model.NBlockModel
import live.lingting.minecraft.template.PanelOreBlock
import live.lingting.minecraft.textures.Textures

/**
 * @author lingting 2025/11/23 0:48
 */
class PanelOreBlockModel : NBlockModel() {

    override val types: List<Class<out BlockSource>> = listOf(PanelOreBlock::class.java)

    override fun register() {
        val model = cubeAll(Textures.BLOCK_PANEL_ORE)
        forAllStates(model)
        simpleItem(model)
    }

}