package live.lingting.minecraft.data

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest

/**
 * @author lingting 2025/11/23 22:44
 */
abstract class ConfiguredFeatureProvider : BasicFeatureProvider<ConfiguredFeature<*, *>>() {

    override val keyPrefix: String = "configured"

    override fun createKey(): ResourceKey<ConfiguredFeature<*, *>> {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, location)
    }

    /**
     * @param target 替换的方块
     * @param size 矿脉大小
     */
    fun registerOreReplace(target: Block, size: Int) = registerOreReplace(target.defaultBlockState(), size)

    /**
     * @param target 替换的方块状态
     * @param size 矿脉大小
     */
    fun registerOreReplace(target: BlockState, size: Int) {
        assert(size in 0..64) { "矿脉大小必须在(0,64)范围内!" }
        // 匹配可以被替换的石头
        val test = TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES)
        val configuration = OreConfiguration(
            listOf(
                // 把匹配的方块替换为目标方块
                OreConfiguration.target(test, target)
            ), size
        )
        val feature = ConfiguredFeature(Feature.ORE, configuration)
        context.register(key, feature)
    }

}