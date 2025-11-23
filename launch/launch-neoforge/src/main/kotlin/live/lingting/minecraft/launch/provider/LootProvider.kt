package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.data.BasicDataProvider
import live.lingting.minecraft.loot.BlockLootProvider
import net.minecraft.core.HolderLookup
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import java.util.function.BiConsumer

/**
 * @author lingting 2025/11/21 16:49
 */
@Suppress("UNCHECKED_CAST")
class LootProvider(
    e: GatherDataEvent,
    list: List<SubProviderEntry>,
) : LootTableProvider(e.generator.packOutput, setOf(), list, e.lookupProvider) {

    companion object {

        fun register(
            e: GatherDataEvent,
            lootClasses: List<Class<LootTableSubProvider>>,
            registerItems: List<DeferredItem<Item>>,
            registerBlocks: List<DeferredBlock<Block>>
        ) {
            val blockClasses = mutableListOf<Class<BlockLootProvider>>()
            val entityClasses = mutableListOf<Class<LootTableSubProvider>>()

            lootClasses.forEach {
                if (isSuper(it, BlockEntryProvider::class.java)) {
                    return@forEach
                } else if (isSuper(it, BlockLootProvider::class.java)) {
                    blockClasses.add(it as Class<BlockLootProvider>)
                } else {
                    entityClasses.add(it)
                }
            }

            val list = listOf(
                SubProviderEntry({ provider ->
                    BlockEntryProvider(provider, blockClasses).apply {
                        setItems { registerItems.map { it.get() } }
                        setBlocks { registerBlocks.map { it.get() } }
                    }
                }, LootContextParamSets.BLOCK)
            )


            if (entityClasses.isNotEmpty()) {
                TODO("要实现实体的战利品列表注册")
            }
            e.generator.addProvider(true, LootProvider(e, list))
        }

    }

    class BlockEntryProvider(
        val provider: HolderLookup.Provider,
        val lootClasses: List<Class<BlockLootProvider>>
    ) : BlockLootProvider(provider) {

        override fun generate(biConsumer: BiConsumer<ResourceKey<LootTable?>?, LootTable.Builder?>) {
            lootClasses.forEach { lootCls ->
                val also = ClassUtils.newInstance(lootCls, true, listOf(provider))
                    .also { ltp ->
                        if (ltp.isSuper(BasicDataProvider::class)) {
                            val p = ltp as BasicDataProvider
                            p.setItems { items }
                            p.setBlocks { blocks }
                        }
                    }

                also.generate(biConsumer)
                also.transfer(this)
            }

            superGenerate(biConsumer)
        }

        override fun generate() {
            // 无实现, 放在了 generate 调用父级之前 由内部的其他provider实现生成, 这里只做汇总
        }

        /**
         * 重写 neoforge 自己新增的方法, 返回模组的物品
         */
        override fun getKnownBlocks(): Iterable<Block> {
            return blocks
        }

    }

}