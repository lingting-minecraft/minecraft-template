package live.lingting.minecraft.data

import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.placement.PlacementModifier

/**
 * @author lingting 2025/11/23 22:44
 */
abstract class PlacedFeatureProvider : BasicFeatureProvider<PlacedFeature>() {

    override val keyPrefix: String = "placed"

    override fun createKey(): ResourceKey<PlacedFeature> {
        return ResourceKey.create(Registries.PLACED_FEATURE, location)
    }

    protected val features: HolderGetter<ConfiguredFeature<*, *>>
        get() = context.lookup(Registries.CONFIGURED_FEATURE)

    fun register(configKey: ResourceKey<ConfiguredFeature<*, *>>, list: List<PlacementModifier>) {
        val reference = features.getOrThrow(configKey)
        val feature = PlacedFeature(reference, list)
        context.register(key, feature)
    }

}