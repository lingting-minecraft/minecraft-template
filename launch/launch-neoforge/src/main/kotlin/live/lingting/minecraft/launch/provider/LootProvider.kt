package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.data.BasicDataProvider
import live.lingting.minecraft.data.RegisterData
import live.lingting.minecraft.loot.BlockLootProvider
import net.minecraft.core.HolderLookup
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.neoforged.neoforge.data.event.GatherDataEvent
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
            lootClasses: List<Class<out LootTableSubProvider>>, registerData: RegisterData
        ) {
            val blockClasses = mutableListOf<Class<out BlockLootProvider>>()
            // todo 自定义的实体类型
            val entityClasses = mutableListOf<Class<out LootTableSubProvider>>()
            // 非内部类型
            val otherClasses = mutableListOf<Class<out LootTableSubProvider>>()

            lootClasses.forEach {
                if (isSuper(it, BlockEntryProvider::class.java)) {
                    return@forEach
                } else if (isSuper(it, BlockLootProvider::class.java)) {
                    blockClasses.add(it as Class<BlockLootProvider>)
                } else {
                    entityClasses.add(it)
                }
            }

            val list: List<SubProviderEntry> = buildList {
                add(
                    SubProviderEntry({ provider ->
                        BlockEntryProvider(provider, blockClasses).apply {
                            this.registerData = registerData
                        }
                    }, LootContextParamSets.BLOCK)
                )

                otherClasses.forEach {
                    val set = if (isSuper(it, BlockLootSubProvider::class.java)) {
                        LootContextParamSets.BLOCK
                    } else {
                        LootContextParamSets.ENTITY
                    }
                    val entry = SubProviderEntry({ provider ->
                        ClassUtils.newInstance(it, false, listOf(provider))
                    }, set)
                    add(entry)
                }
            }



            if (entityClasses.isNotEmpty()) {
                TODO("要实现实体的战利品列表注册")
            }
            e.generator.addProvider(true, LootProvider(e, list))
        }

    }

    class BlockEntryProvider(
        val provider: HolderLookup.Provider,
        val lootClasses: List<Class<out BlockLootProvider>>
    ) : BlockLootProvider(provider) {

        override fun generate(biConsumer: BiConsumer<ResourceKey<LootTable?>?, LootTable.Builder?>) {
            lootClasses.forEach { lootCls ->
                val also = ClassUtils.newInstance(lootCls, true, listOf(provider))
                    .also { ltp ->
                        if (ltp.isSuper(BasicDataProvider::class)) {
                            val p = ltp as BasicDataProvider
                            p.registerData = registerData
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