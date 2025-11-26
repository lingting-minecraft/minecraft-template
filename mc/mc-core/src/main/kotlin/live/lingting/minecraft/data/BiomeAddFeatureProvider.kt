package live.lingting.minecraft.data

import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.placement.PlacedFeature

/**
 * @author lingting 2025/11/26 22:47
 */
abstract class BiomeAddFeatureProvider : BasicFeatureProvider<Any>() {

    override val keyPrefix: String = "biome_add_feature"

    override fun createKey(): ResourceKey<Any> {
        throw UnsupportedOperationException("调用了不该调用的方法")
    }

    override fun register() {
        createKey()
    }

    /**
     * 在什么阶段添加
     */
    abstract val decoration: GenerationStep.Decoration

    /**
     * 在哪些群系添加
     */
    abstract fun tag(): TagKey<Biome>

    /**
     * 添加哪些配置
     */
    abstract fun keys(): List<ResourceKey<PlacedFeature>>

}