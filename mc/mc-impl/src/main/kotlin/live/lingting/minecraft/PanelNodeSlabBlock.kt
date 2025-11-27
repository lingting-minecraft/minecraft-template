package live.lingting.minecraft

import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.block.BlockStatePredicates.ALWAY_TRUE
import live.lingting.minecraft.block.ISlabBlock
import live.lingting.minecraft.loot.BlockLootProvider
import live.lingting.minecraft.recipes.ArrayRecipeProvider
import net.minecraft.core.HolderLookup
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument

/**
 * 台阶方块
 * @author lingting 2025/11/27 15:07
 */
class PanelNodeSlabBlock : ISlabBlock {

    companion object {

        @JvmField
        val ID = BlockSource.id("panel.node.slab")

    }

    constructor(p: Properties) : super(p.apply {
        // 要求指定工具
        requiresCorrectToolForDrops()
            // 点击, 挖掘音效
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.STONE)
            // 基础挖掘耗时: 秒, 爆炸抗性
            .strength(1.5f, 6f)
            // 是否允许红石信号穿过
            .isRedstoneConductor(ALWAY_TRUE)
            // 透明不完全阻挡视线
            .noOcclusion()
            // 碰撞箱动态变化
            .dynamicShape()
            // 设置不同状态时不同的亮度, 这里统一
            .lightLevel {
                5
            }
    })

    override val tags: List<TagKey<Block>> = listOf(BlockTags.MINEABLE_WITH_PICKAXE)

    /**
     * 挖掘时的战利品掉落声明
     */
    class Loot : BlockLootProvider {

        constructor(provider: HolderLookup.Provider) : super(provider)

        override fun generate() {
            val block = getBlock(ID)
            dropSlabItemTable(block)
        }

    }

    class Recipe : ArrayRecipeProvider() {

        override fun all(): Collection<RecipeBuilder> {
            val block = getBlock(ID)
            val source = getBlock(PanelNodeBlock.ID)
            return buildList {
                add(slabBlock(source, block))
            }
        }

    }

}