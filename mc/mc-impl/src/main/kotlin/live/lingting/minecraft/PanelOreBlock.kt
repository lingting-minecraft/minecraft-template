package live.lingting.minecraft

import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.component.range.FloatRange
import live.lingting.minecraft.data.BiomeAddFeatureProvider
import live.lingting.minecraft.data.ConfiguredFeatureProvider
import live.lingting.minecraft.data.PlacedFeatureProvider
import live.lingting.minecraft.kt.number
import live.lingting.minecraft.loot.BlockLootProvider
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BiomeTags
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.placement.BiomeFilter
import net.minecraft.world.level.levelgen.placement.CountPlacement
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement
import net.minecraft.world.level.levelgen.placement.InSquarePlacement
import net.minecraft.world.level.levelgen.placement.PlacedFeature

/**
 * @author lingting 2025/11/22 22:21
 */
class PanelOreBlock : IBlock {

    companion object {

        @JvmField
        val ID = BlockSource.id("panel.ore")

    }

    constructor(p: Properties) : super(p.apply {
        // 要求指定工具
        requiresCorrectToolForDrops()
            // 点击, 挖掘音效
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.STONE)
            // 基础挖掘耗时: 秒, 爆炸抗性
            .strength(2f, 3f)
            // 设置不同状态时不同的亮度, 这里统一
            .lightLevel {
                13
            }
    })

    /**
     * 声明物品尅用哪些工具, 这里同时支持 斧头和稿子. 需要配合 requiresCorrectToolForDrops()
     */
    override val tags: List<TagKey<Block>> = listOf(BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_PICKAXE)

    /**
     * 自然生成配置, 用于声明把哪些方块的生成替换成指定方块, 以及指定数量等
     */
    class Configured : ConfiguredFeatureProvider() {

        override val id: String = ID

        override fun register() {
            registerOreReplace(getBlock(ID), 7)
        }

    }

    /**
     * 自然生成放置策略, 用于声明指定配置生效的条件, 包括 群系, 尝试次数, 范围等
     */
    class Placed(val configured: Configured) : PlacedFeatureProvider() {

        override val id: String = ID

        override fun register() {
            register(
                configured.key,
                listOf(
                    // 每区块生成 10~16 次尝试
                    CountPlacement.of(UniformInt.of(2, 4)),
                    InSquarePlacement.spread(),
                    // y=-64 到 y=64
                    HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(64)),
                    // 所有生物群落
                    BiomeFilter.biome()
                )
            )
        }

    }

    /**
     * 在矿物生成阶段, 往主世界添加 当前方块的矿物, 使用指定的放置策略.
     */
    class BiomeAddFeature(val placed: Placed) : BiomeAddFeatureProvider() {

        override val id: String = ID

        override val decoration: GenerationStep.Decoration = GenerationStep.Decoration.UNDERGROUND_ORES

        override fun tag(): TagKey<Biome> = BiomeTags.IS_OVERWORLD

        override fun keys(): List<ResourceKey<PlacedFeature>> = listOf(placed.key)

    }

    /**
     * 挖掘时的战利品掉落声明
     */
    class Loot : BlockLootProvider {

        constructor(provider: HolderLookup.Provider) : super(provider)

        override fun generate() {
            val block = getBlock(ID)
            val item = getBlock(PanelNodeBlock.ID)
            dropNormal(block, item, FloatRange(1f, 9f).number)
        }

    }

}