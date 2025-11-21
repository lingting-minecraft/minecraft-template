package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.loot.BlockLootProvider
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem

/**
 * @author lingting 2025/11/21 16:49
 */
class LootProvider(
    e: GatherDataEvent,
    list: List<SubProviderEntry>,
) : LootTableProvider(e.generator.packOutput, setOf(), list, e.lookupProvider) {

    companion object {

        fun register(
            e: GatherDataEvent,
            lootClasses: MutableList<Class<LootTableSubProvider>>,
            registerItems: List<DeferredItem<Item>>,
            registerBlocks: List<DeferredBlock<Block>>
        ) {
            val list = mutableListOf<SubProviderEntry>()

            lootClasses.forEach { lootCls ->
                val set = if (isSuper(lootCls, BlockLootSubProvider::class.java)) {
                    LootContextParamSets.BLOCK
                } else {
                    LootContextParamSets.ENTITY
                }

                val entry = SubProviderEntry({ provider ->
                    ClassUtils.newInstance(lootCls, true, listOf(provider))
                        .also { ltp ->
                            if (ltp.isSuper(BlockLootProvider::class)) {
                                val p = ltp as BlockLootProvider
                                p.setItems { registerItems.map { it.get() } }
                                p.setBlocks { registerBlocks.map { it.get() } }
                            }
                        }
                }, set)
                list.add(entry)
            }

            if (list.isNotEmpty()) {
                e.generator.addProvider(true, LootProvider(e, list))
            }
        }

    }

}