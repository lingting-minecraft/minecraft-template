package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.App.modId
import live.lingting.minecraft.component.value.ClassNewValue
import live.lingting.minecraft.data.BasicFeatureProvider
import live.lingting.minecraft.data.BiomeAddFeatureProvider
import live.lingting.minecraft.data.ConfiguredFeatureProvider
import live.lingting.minecraft.data.PlacedFeatureProvider
import net.minecraft.core.HolderSet
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import net.neoforged.neoforge.common.world.BiomeModifier
import net.neoforged.neoforge.common.world.BiomeModifiers
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.registries.NeoForgeRegistries

/**
 * @author lingting 2025/11/23 22:47
 */
@Suppress("UNCHECKED_CAST")
class DatapackProvider : DatapackBuiltinEntriesProvider {

    companion object {

        val log = logger()

        fun register(
            e: GatherDataEvent,
            classes: List<Class<out BasicFeatureProvider<*>>>
        ) {
            e.addProvider(DatapackProvider(e, classes))
        }

    }

    constructor(
        e: GatherDataEvent,
        classes: List<Class<out BasicFeatureProvider<*>>>
    ) : super(
        e.generator.packOutput,
        e.lookupProvider,
        Builder(classes),
        setOf("minecraft", modId)
    )

    class Builder(
        classes: List<Class<out BasicFeatureProvider<*>>>
    ) : RegistrySetBuilder() {

        val instances: List<BasicFeatureProvider<*>>

        init {
            val value = ClassNewValue(classes, listOf())
            instances = value.create()
            add(Registries.CONFIGURED_FEATURE, ::configured)
            add(Registries.PLACED_FEATURE, ::place)
            add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ::biomeModifier)
        }

        fun <T> register(cls: Class<out BasicFeatureProvider<T>>, context: BootstrapContext<T>) {
            instances.mapNotNull {
                if (isSuper(it, cls)) {
                    it as BasicFeatureProvider<T>
                } else {
                    null
                }
            }.forEach {
                it.context = context
                it.register()
                log.debug("[{}] 注册功能完成. id: {}; 类: {}; ", it.keyPrefix, it.id, it.javaClass.name)
            }
        }

        fun configured(context: BootstrapContext<ConfiguredFeature<*, *>>) {
            register(ConfiguredFeatureProvider::class.java, context)
        }

        fun place(context: BootstrapContext<PlacedFeature>) {
            register(PlacedFeatureProvider::class.java, context)
        }

        fun biomeModifier(context: BootstrapContext<BiomeModifier>) {
            instances.mapNotNull {
                if (isSuper(it, BiomeAddFeatureProvider::class.java)) {
                    it as BiomeAddFeatureProvider
                } else {
                    null
                }
            }.forEach {
                val biome = context.lookup(Registries.BIOME)
                val feature = context.lookup(Registries.PLACED_FEATURE)

                val key = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, it.location)
                val biomes = biome.getOrThrow(it.tag())
                val features = HolderSet.direct(it.keys().map { k -> feature.getOrThrow(k) })
                context.register(key, BiomeModifiers.AddFeaturesBiomeModifier(biomes, features, it.decoration))
            }
        }

    }

}