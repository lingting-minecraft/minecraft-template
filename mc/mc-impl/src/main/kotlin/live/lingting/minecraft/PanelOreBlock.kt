package live.lingting.minecraft

import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.component.range.FloatRange
import live.lingting.minecraft.loot.BlockLootProvider
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.storage.loot.LootTable

/**
 * @author lingting 2025/11/22 22:21
 */
class PanelOreBlock : IBlock {

    companion object {

        @JvmField
        val ID = id("panel.ore")

    }

    constructor(p: Properties) : super(p.apply {
        // 要求指定工具
        requiresCorrectToolForDrops()
            // 点击, 挖掘音效
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.STONE)
            // 基础挖掘耗时: 秒, 爆炸抗性
            .strength(2f, 3f)
            // 矿石设置不同状态时不同的亮度, 这里统一
            .lightLevel {
                13
            }
    })

    /**
     * 声明物品尅用哪些工具, 这里同时支持 斧头和稿子. 需要配合 requiresCorrectToolForDrops()
     */
    override val tags: List<TagKey<Block>> = listOf(BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_PICKAXE)

    class Loot : BlockLootProvider {

        constructor(provider: HolderLookup.Provider) : super(provider)

        override fun generate() {
            val block = getBlock(ID)
            val table = LootTable.lootTable()
                // 精准采集 - 获取一个自己
                .withPool(createSinglePool(block).`when`(hasSilkTouch()))
                // 其他工具 - 获取掉落物
                .withPool(
                    createRangePool(getBlock(PanelNodeBlock.ID), FloatRange(1f, 9f))
                        .`when`(doesNotHaveSilkTouch())
                )
            add(block, table)
        }

    }

}