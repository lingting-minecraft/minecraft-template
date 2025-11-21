package live.lingting.minecraft.launch.item

import live.lingting.minecraft.PanelItem
import live.lingting.minecraft.launch.model.NItemModel
import live.lingting.minecraft.textures.Textures
import live.lingting.minecraft.world.IWorld

class PanelItemModel : NItemModel() {

    override val types: List<Class<out IWorld>> = listOf(PanelItem::class.java)

    override fun register() {
        layer0(Textures.ITEM_PANEL)
    }

}