package live.lingting.minecraft

import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.eunums.ActiveEnum
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.loot.BlockLootProvider
import live.lingting.minecraft.recipes.ArrayRecipeProvider
import net.minecraft.ChatFormatting
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
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
import java.util.concurrent.CompletableFuture

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

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: Item.TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
        super.appendHoverText(stack, tooltip, components, flag)
        components.add(I18n.BLOCK.PANEL_NODE_HOVER.translatable(stack.count).withStyle(ChatFormatting.GOLD))
    }

    class Loot : BlockLootProvider {

        constructor(provider: HolderLookup.Provider) : super(provider)

        override fun generate() {
            val block = getBlock(ID)
            dropSelf(block)
        }

    }

    class Recipe(o: PackOutput, f: CompletableFuture<HolderLookup.Provider>) : ArrayRecipeProvider(o, f) {

        override val prefix = ID

        override fun builderes(): Collection<RecipeBuilder> {
            val block = getBlock(ID)
            return buildList {
                add(
                    // 任意位置的3个砂砾生成一个面板节点
                    ShapelessRecipeBuilder
                        .shapeless(RecipeCategory.BUILDING_BLOCKS, block, 1)
                        .requires(Items.GRAVEL, 3)
                        // 仅在拥有砂砾是解锁
                        .unlockedBy("has_gravel", has(Items.GRAVEL))
                )
            }
        }

    }

}