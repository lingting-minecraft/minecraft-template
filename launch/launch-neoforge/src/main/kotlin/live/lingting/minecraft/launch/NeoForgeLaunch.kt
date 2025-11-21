package live.lingting.minecraft.launch

import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.App.modId
import live.lingting.minecraft.App.resourceName
import live.lingting.minecraft.PanelItem
import live.lingting.minecraft.PanelNodeBlock
import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.block.IBlockEntity
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.item.IItem
import live.lingting.minecraft.launch.basic.NBlockEntityHolder
import live.lingting.minecraft.launch.listener.NeoForgeLeftClickListener
import live.lingting.minecraft.launch.provider.LanguageProvider
import live.lingting.minecraft.launch.provider.LanguageProvider.Companion.translatable
import live.lingting.minecraft.launch.provider.LootProvider
import live.lingting.minecraft.launch.provider.ModRecipeProvider
import live.lingting.minecraft.launch.provider.ModelProvider
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

    val tab: DeferredHolder<CreativeModeTab, CreativeModeTab>

    val items = DeferredRegister.createItems(modId)

    val blocks = DeferredRegister.createBlocks(modId)

    val blockEntityTypes = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modId)

    init {
        onInitializer()

        blocks.register(bus)
        items.register(bus)
        blockEntityTypes.register(bus)

        val create = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId)
        tab = create.register(resourceName, Supplier {
            CreativeModeTab.builder()
                .title(I18n.MOD_TITLE.translatable())
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon { baseItem.get().defaultInstance }
                .build()
        })
        create.register(bus)
        bus.addListener(::onTab)
        bus.addListener(::onClientGatherData)

        NeoForge.EVENT_BUS.register(NeoForgeLeftClickListener)
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
        if (e.tabKey != tab.key) {
            return
        }
        log.debug("[{}] register tab", modId)
        registerBlockItems.forEach { e.accept(it) }
        registerItems.forEach { e.accept(it) }
    }

    fun onClientGatherData(e: GatherDataEvent) {
        if (e.includeClient()) {
            LanguageProvider.register(e, registerItems, registerBlocks)
            ModelProvider.register(e, registerItems, registerBlocks)
        }
        if (e.includeServer()) {
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
            LootProvider.register(e, lootClasses, registerItems, registerBlocks)
            ModRecipeProvider.register(e, recipeClasses, registerItems, registerBlocks)
        }
    }

}
