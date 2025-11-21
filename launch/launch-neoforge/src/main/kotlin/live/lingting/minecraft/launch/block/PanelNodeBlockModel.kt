package live.lingting.minecraft.launch.block

import live.lingting.minecraft.PanelNodeBlock
import live.lingting.minecraft.eunums.ActiveEnum
import live.lingting.minecraft.launch.model.NBlockModel
import live.lingting.minecraft.textures.Textures
import live.lingting.minecraft.world.IWorld

class PanelNodeBlockModel : NBlockModel() {

    override val types: List<Class<out IWorld>> = listOf(PanelNodeBlock::class.java)

    override fun register() {
        val model = cubeAll(Textures.BLOCK_PANEL_NODE)
        val activeModel = cubeAll(Textures.BLOCK_PANEL_NODE_ACTIVE, "active")
        forAllStatesByModel {
            val active = it.getValue(ActiveEnum.PROPERTY)
            if (active.isActivated) activeModel else model
        }
        simpleItem(model)
    }

}