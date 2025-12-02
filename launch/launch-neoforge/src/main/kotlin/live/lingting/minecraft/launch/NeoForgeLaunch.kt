package live.lingting.minecraft.launch

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.App.modId
import live.lingting.minecraft.CreativeTabs
import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.block.IBlockEntity
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.data.BasicComponentData
import live.lingting.minecraft.data.BasicFeatureProvider
import live.lingting.minecraft.item.ItemSource
import live.lingting.minecraft.launch.basic.NBlockEntityHolder
import live.lingting.minecraft.launch.bus.NeoForgeClickListener
import live.lingting.minecraft.launch.bus.NeoForgeClientLevelListener
import live.lingting.minecraft.launch.bus.NeoForgeCommand
import live.lingting.minecraft.launch.bus.NeoForgePlayerListener
import live.lingting.minecraft.launch.provider.BlockTagsProvider
import live.lingting.minecraft.launch.provider.DatapackProvider
import live.lingting.minecraft.launch.provider.LanguageProvider
import live.lingting.minecraft.launch.provider.LootProvider
import live.lingting.minecraft.launch.provider.ModelProvider
import live.lingting.minecraft.launch.provider.PackMetaProvider
import live.lingting.minecraft.launch.provider.RecipeProvider
import live.lingting.minecraft.recipes.BasicRecipeProvider
import live.lingting.minecraft.world.IWorld
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.javafmlmod.FMLModContainer
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

/**
 * @author lingting 2025/8/22 17:13
 */
@Suppress("UNCHECKED_CAST")
@Mod("lingting_minecraft_template")
class NeoForgeLaunch(
    val bus: IEventBus,
    val container: ModContainer,
    val fmlContainer: FMLModContainer,
    val type: Dist
) : Launch<DeferredItem<Item>, DeferredBlock<Block>, DeferredItem<BlockItem>>() {

    override val isClient: Boolean
        get() = type == Dist.CLIENT

    val items = DeferredRegister.createItems(modId)

    val blocks = DeferredRegister.createBlocks(modId)

    val blockEntityTypes = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modId)

    val tabMap =
        LinkedHashMap<CreativeTabs, DeferredHolder<CreativeModeTab, CreativeModeTab>>(CreativeTabs.entries.size)

    val numberProvider = DeferredRegister.create(Registries.LOOT_NUMBER_PROVIDER_TYPE, modId)

    val dataComponent = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, modId)

    init {
        onInitializer()

        blocks.register(bus)
        items.register(bus)
        blockEntityTypes.register(bus)

        val tabCreate = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId)
        CreativeTabs.entries.forEach {
            val tab = tabCreate.register(it.id, Supplier {
                CreativeModeTab.builder()
                    .title(it.i18n.translatable())
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(it.icon)
                    .build()
            })
            tabMap[it] = tab
        }
        tabCreate.register(bus)
        numberProvider.register(bus)
        dataComponent.register(bus)
        bus.addListener(::onTab)
        bus.addListener(::onClientGatherData)

        val bus = NeoForge.EVENT_BUS
        bus.register(NeoForgeClickListener)
        bus.register(NeoForgePlayerListener)
        bus.register(NeoForgeClientLevelListener)
        bus.register(NeoForgeCommand(commandClasses))
    }

    override fun registerItem(
        id: String,
        c: Class<out ItemSource>
    ): DeferredItem<Item> {
        val cls = c as Class<Item>
        return items.registerItem(id) { ItemSource.create(cls, it) }
    }

    override fun registerBlock(
        id: String,
        c: Class<out BlockSource>
    ): Pair<DeferredBlock<Block>, DeferredItem<BlockItem>> {
        val cls = c as Class<Block>
        val b = blocks.registerBlock(id) { BlockSource.create(cls, it) }
        val i = items.registerSimpleBlockItem(id, b)
        return Pair(b, i)
    }

    override fun registerBlockEntityMapping(map: Map<Class<out BlockSource>, Class<out IBlockEntity>>) {
        IBlockEntity.registerMapping(map)
    }

    override fun registerBlockEntity(map: Map<Class<out IBlockEntity>, List<DeferredBlock<Block>>>) {
        map.forEach { (clazz, blocks) ->
            val holder = NBlockEntityHolder(clazz, blockEntityTypes) { blocks.map { it.get() } }
            IBlockEntity.register(holder)
        }
    }

    override fun <T : NumberProvider> registerNumberProvider(
        name: String,
        codec: MapCodec<T>
    ): Supplier<LootNumberProviderType> {
        return numberProvider.register(name, Supplier { LootNumberProviderType(codec) })
    }

    override fun <T : BasicComponentData> registerComponentData(
        name: String,
        codec: Codec<T>,
        streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T>?
    ): Supplier<DataComponentType<T>> {
        return dataComponent.register(name, Supplier {
            val builder = DataComponentType.builder<T>()
                .cacheEncoding()
                .persistent(codec)
            if (streamCodec != null) {
                builder.networkSynchronized(streamCodec)
            }
            builder.build()
        })
    }

    fun onTab(e: BuildCreativeModeTabContentsEvent) {
        log.debug("[{}] register tab: {}", modId, e.tabKey)
        if (tabMap.isEmpty()) {
            return
        }
        val keys = tabMap.keys
        val first = keys.first()
        val onIWorld = { i: IWorld ->
            val tabId = i.creativeTabId
            val key = keys.firstOrNull { tabId.isNullOrBlank() || it.id == tabId } ?: first
            val tab = tabMap[key]
            if (tab?.key == e.tabKey) {
                e.accept(i as ItemLike)
            }
        }

        registerBlockItems.forEach {
            val block = it.get().block
            if (block.isSuper(IWorld::class)) {
                onIWorld(block as IWorld)
            }
        }

        registerItems.forEach {
            val item = it.get()
            if (item.isSuper(IWorld::class)) {
                onIWorld(item as IWorld)
            }
        }

    }

    /**
     * 不区分客户端和服务端, 这样子一个jar两边都能用
     * 缺点: 服务端jar比预期要大很多(客户端资源也打进去了)
     */
    fun onClientGatherData(e: GatherDataEvent) {
        PackMetaProvider.register(e)
        LanguageProvider.register(e, registerItems, registerBlocks)
        ModelProvider.register(e, registerItems, registerBlocks)

        val lootClasses = mutableListOf<Class<out LootTableSubProvider>>()
        val recipeClasses = mutableListOf<Class<out BasicRecipeProvider>>()
        val featureProviderClasses = mutableListOf<Class<out BasicFeatureProvider<*>>>()
        dataProviderClasses.forEach {
            if (isSuper(it, LootTableSubProvider::class.java)) {
                lootClasses.add(it as Class<out LootTableSubProvider>)
            }
            if (isSuper(it, BasicRecipeProvider::class.java)) {
                recipeClasses.add(it as Class<out BasicRecipeProvider>)
            }
            if (isSuper(it, BasicFeatureProvider::class.java)) {
                featureProviderClasses.add(it as Class<out BasicFeatureProvider<*>>)
            }
        }
        // 下面这些数据, 客户端和服务端都需要包含
        LootProvider.register(e, lootClasses)
        RecipeProvider.register(e, recipeClasses)
        BlockTagsProvider.register(e)
        DatapackProvider.register(e, featureProviderClasses)
    }

}
