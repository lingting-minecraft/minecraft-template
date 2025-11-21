package live.lingting.minecraft

import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.eunums.ActiveEnum
import live.lingting.minecraft.loot.BlockLootProvider
import net.minecraft.core.HolderLookup
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition

/**
 * @author lingting 2025/11/6 19:05
 */
class PanelNodeBlock : IBlock {

    companion object {

        @JvmField
        val ID = id("panel.node")

    }

    constructor(p: Properties) : super(p) {
        registerDefaultState(stateDefinition.any().setValue(ActiveEnum.PROPERTY, ActiveEnum.CREATED))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        super.createBlockStateDefinition(builder)
        builder.add(ActiveEnum.PROPERTY)
    }

    class Loot : BlockLootProvider {

        constructor(provider: HolderLookup.Provider) : super(provider)

        override fun generate() {
            blocks.filter { it.isSuper(PanelNodeBlock::class) }
                .forEach {
                    dropSelf(it)
                }
        }

    }

}