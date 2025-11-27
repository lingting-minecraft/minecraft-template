package live.lingting.minecraft

import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.eunums.ActiveEnum
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.loot.BlockLootProvider
import live.lingting.minecraft.recipes.ArrayRecipeProvider
import net.minecraft.ChatFormatting
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapelessRecipeBuilder
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition

/**
 * @author lingting 2025/11/6 19:05
 */
class PanelNodeBlock : IBlock {

    companion object {

        @JvmField
        val ID = BlockSource.id("panel.node")

    }

    constructor(p: Properties) : super(p) {
        registerDefaultState(stateDefinition.any().setValue(ActiveEnum.PROPERTY, ActiveEnum.CREATED))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder.add(ActiveEnum.PROPERTY)
    }

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: Item.TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
        var active: ActiveEnum? = null
        stack.componentsPatch?.get(DataComponents.BLOCK_STATE)?.ifPresent {
            active = it.get(ActiveEnum.PROPERTY)
        }
        if (active != null && active != ActiveEnum.CREATED) {
            var count = 0L
            stack.componentsPatch?.get(DataComponents.BLOCK_ENTITY_DATA)?.ifPresent {
                val tag = it.copyTag()
                count = tag.getLong(PanelNodeBlockEntity.TAG_COUNTER)
            }
            val component = I18n.BLOCK.PANEL_NODE_HOVER.translatable(count)
            val formatting = if (active!!.isActivated) ChatFormatting.GOLD else ChatFormatting.GRAY
            components.add(component.withStyle(formatting))
        }
    }

    class Loot : BlockLootProvider {

        constructor(provider: HolderLookup.Provider) : super(provider)

        override fun generate() {
            val block = getBlock(ID)
            dropCopy(block)
        }

    }

    class Recipe : ArrayRecipeProvider() {

        override fun all(): Collection<RecipeBuilder> {
            val block = getBlock(ID)
            return buildList {
                add(
                    // 任意位置的3个沙砾生成一个面板节点
                    ShapelessRecipeBuilder
                        .shapeless(RecipeCategory.BUILDING_BLOCKS, block, 1)
                        .requires(Items.GRAVEL, 3)
                        // 仅在拥有沙砾是解锁
                        .unlockedBy("has_gravel", has(Items.GRAVEL))
                )
            }
        }

    }

}