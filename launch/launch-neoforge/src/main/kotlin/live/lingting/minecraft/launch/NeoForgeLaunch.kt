package live.lingting.minecraft.launch

import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.App.modId
import live.lingting.minecraft.PanelItem
import live.lingting.minecraft.PanelNodeBlock
import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.block.IBlockEntity
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.eunums.CreativeTabs
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.item.IItem
import live.lingting.minecraft.launch.basic.NBlockEntityHolder
import live.lingting.minecraft.launch.bus.NeoForgeCommand
import live.lingting.minecraft.launch.bus.NeoForgeLeftClickListener
import live.lingting.minecraft.launch.provider.BlockTagsProvider
import live.lingting.minecraft.launch.provider.LanguageProvider
import live.lingting.minecraft.launch.provider.LootProvider
import live.lingting.minecraft.launch.provider.ModRecipeProvider
import live.lingting.minecraft.launch.provider.ModelProvider
import live.lingting.minecraft.world.IWorld
import net.minecraft.core.registries.Registries
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
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
import java.util.EnumMap
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

    override val baseItemId: String by lazy { PanelItem.ID }

    override val baseBlockId: String by lazy { PanelNodeBlock.ID }

    val items = DeferredRegister.createItems(modId)

    val blocks = DeferredRegister.createBlocks(modId)

    val blockEntityTypes = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modId)

    val tabMap = EnumMap<CreativeTabs, DeferredHolder<CreativeModeTab, CreativeModeTab>>(CreativeTabs::class.java)

    init {
        onInitializer()

        blocks.register(bus)
        items.register(bus)
        blockEntityTypes.register(bus)

        val tabCreate = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId)
        CreativeTabs.entries.forEach {
            val tab = tabCreate.register(it.id, Supplier {
                CreativeModeTab.builder()
                    .title(I18n.MOD_TITLE.translatable())
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon { baseItem.get().defaultInstance }
                    .build()
            })
            tabMap[it] = tab
        }
        tabCreate.register(bus)
        bus.addListener(::onTab)
        bus.addListener(::onClientGatherData)

        val bus = NeoForge.EVENT_BUS
        bus.register(NeoForgeLeftClickListener)
        bus.register(NeoForgeCommand(commandClasses))
    }

    override fun registerItem(
        id: String,
        c: Class<out IItem>
    ): DeferredItem<Item> {
        val cls = c as Class<Item>
        return items.registerItem(id) { IItem.create(cls, it) }
    }

    override fun registerBlock(
        id: String,
        c: Class<out IBlock>
    ): Pair<DeferredBlock<Block>, DeferredItem<BlockItem>> {
        val cls = c as Class<Block>
        val b = blocks.registerBlock(id) { IBlock.create(cls, it) }
        val i = items.registerSimpleBlockItem(id, b)
        return Pair(b, i)
    }

    override fun registerBlockEntityMapping(map: Map<Class<out IBlock>, Class<out IBlockEntity>>) {
        IBlockEntity.registerMapping(map)
    }

    override fun registerBlockEntity(map: Map<Class<out IBlockEntity>, List<DeferredBlock<Block>>>) {
        map.forEach { (clazz, blocks) ->
            val holder = NBlockEntityHolder(clazz, blockEntityTypes) { blocks.map { it.get() } }
            IBlockEntity.register(holder)
        }
    }

    fun onTab(e: BuildCreativeModeTabContentsEvent) {
        log.debug("[{}] register tab: {}", modId, e.tabKey)

        registerBlockItems.forEach {
            val block = it.get().block
            if (block.isSuper(IWorld::class)) {
                val tab = tabMap[(block as IWorld).creativeTab]
                if (tab?.key == e.tabKey) {
                    e.accept(it)
                }
            }
        }

        registerItems.forEach {
            val item = it.get()
            if (item.isSuper(IWorld::class)) {
                val tab = tabMap[(item as IWorld).creativeTab]
                if (tab?.key == e.tabKey) {
                    e.accept(it)
                }
            }
        }

    }

    /**
     * 不区分客户端和服务端, 这样子一个jar两边都能用
     * 缺点: 服务端jar比预期要大很多(客户端资源也打进去了)
     */
    fun onClientGatherData(e: GatherDataEvent) {
        LanguageProvider.register(e, registerItems, registerBlocks)
        ModelProvider.register(e, registerItems, registerBlocks)

        val lootClasses = mutableListOf<Class<LootTableSubProvider>>()
        val recipeClasses = mutableListOf<Class<RecipeProvider>>()
        dataProviderClasses.forEach {
            if (isSuper(it, LootTableSubProvider::class.java)) {
                lootClasses.add(it as Class<LootTableSubProvider>)
            }
            if (isSuper(it, RecipeProvider::class.java)) {
                recipeClasses.add(it as Class<RecipeProvider>)
            }
        }
        // 下面这些数据, 客户端和服务端都需要包含
        LootProvider.register(e, lootClasses, registerItems, registerBlocks)
        ModRecipeProvider.register(e, recipeClasses, registerItems, registerBlocks)
        BlockTagsProvider.register(e, registerBlocks)
    }

}
