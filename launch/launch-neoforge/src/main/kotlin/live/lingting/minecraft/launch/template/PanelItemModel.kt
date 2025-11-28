package live.lingting.minecraft.launch.template

import live.lingting.minecraft.item.ItemSource
import live.lingting.minecraft.launch.model.NItemModel
import live.lingting.minecraft.template.PanelItem
import live.lingting.minecraft.textures.Textures

class PanelItemModel : NItemModel() {

    override val types: List<Class<out ItemSource>> = listOf(PanelItem::class.java)

    override fun register() {
        layer0(Textures.ITEM_PANEL)
    }

}