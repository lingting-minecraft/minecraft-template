package live.lingting.minecraft.launch.template

import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.eunums.ActiveEnum
import live.lingting.minecraft.launch.model.NBlockModel
import live.lingting.minecraft.template.PanelNodeBlock
import live.lingting.minecraft.template.PanelNodeSlabBlock
import live.lingting.minecraft.template.PanelNodeStairBlock
import live.lingting.minecraft.template.PanelNodeWallBlock
import live.lingting.minecraft.textures.Textures

class PanelNodeBlockModel : NBlockModel() {

    override val types: List<Class<out BlockSource>>
        get() = listOf(
            PanelNodeBlock::class.java,
            PanelNodeSlabBlock::class.java,
            PanelNodeStairBlock::class.java,
            PanelNodeWallBlock::class.java
        )

    override fun register() {
        if (block.isSuper(PanelNodeBlock::class)) {
            val model = cubeAll(Textures.BLOCK_PANEL_NODE)
            val activeModel = cubeAll(Textures.BLOCK_PANEL_NODE_ACTIVE, "active")
            forAllStatesByModel {
                val active = it.getValue(ActiveEnum.PROPERTY)
                if (active.isActivated) activeModel else model
            }
            simpleItem(model)
        } else if (block.isSuper(PanelNodeSlabBlock::class)) {
            slab(Textures.BLOCK_PANEL_NODE_SLAB)
        } else if (block.isSuper(PanelNodeStairBlock::class)) {
            stair(Textures.BLOCK_PANEL_NODE_STAIR)
        } else if (block.isSuper(PanelNodeWallBlock::class)) {
            wall(Textures.BLOCK_PANEL_NODE_WALL)
        }
    }

}