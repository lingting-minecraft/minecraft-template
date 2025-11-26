package live.lingting.minecraft.loot

import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.component.range.FloatRange
import live.lingting.minecraft.data.RegisterData
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
import java.util.function.BiConsumer

/**
 * @author lingting 2025/11/21 16:35
 */
abstract class BlockLootProvider : BlockLootSubProvider, BasicLootProvider {

    constructor(provider: HolderLookup.Provider) : this(setOf(), FeatureFlags.REGISTRY.allFlags(), provider)

    constructor(set: Set<Item>, featureFlagSet: FeatureFlagSet, provider: HolderLookup.Provider) : super(
        set,
        featureFlagSet,
        provider
    )

    private val registerDataValue = WaitValue.of<RegisterData>()

    override var registerData: RegisterData
        get() = registerDataValue.notNull()
        set(value) = registerDataValue.update(value)

    // region mc

    override fun generate(biConsumer: BiConsumer<ResourceKey<LootTable?>?, LootTable.Builder?>) {
        generate()
    }

    /**
     * 让子类用于调用原始的生成函数
     */
    protected fun superGenerate(biConsumer: BiConsumer<ResourceKey<LootTable?>?, LootTable.Builder?>) {
        super.generate(biConsumer)
    }

    private val cache = mutableMapOf<Block, LootTable.Builder>()

    public override fun add(block: Block, builder: LootTable.Builder) {
        cache.put(block, builder)
        super.add(block, builder)
    }

    /**
     * 把当前声明的战利品列表转移
     */
    fun transfer(provider: BlockLootProvider) {
        cache.forEach { (k, v) ->
            provider.add(k, v)
        }
        cache.clear()
        map.clear()
    }

    // endregion

    fun createCopyEntry(item: ItemLike): LootPoolSingletonContainer.Builder<*> {
        return LootItem.lootTableItem(item)
            .apply(
                CopyComponentsFunction
                    .copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                    .include(DataComponents.BLOCK_ENTITY_DATA)
                    .include(DataComponents.BLOCK_STATE)
            )
    }

    fun createSingleCopyPool(item: ItemLike): LootPool.Builder {
        val entry = createCopyEntry(item)
        return LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1.0F))
            .add(entry)
    }

    fun createSingleCopyTable(item: ItemLike): LootTable.Builder {
        val pool = createSingleCopyPool(item)
        return LootTable.lootTable().withPool(pool)
    }

    /**
     * 掉落一个传入的完整复制
     */
    fun dropCopy(block: Block) {
        val table = createSingleCopyTable(block)
        add(block, table)
    }

    /**
     * 仅在精准采集时掉落一个完全复制的自身
     */
    fun dropCopyWhenSilkTouch(block: Block) {
        val pool = createSingleCopyPool(block)
            .`when`(hasSilkTouch())
        val table = LootTable.lootTable().withPool(pool)
        add(block, table)
    }

    fun createSinglePool(item: ItemLike): LootPool.Builder {
        val entry = LootItem.lootTableItem(item)
        return LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1.0F))
            .add(entry)
    }

    /**
     * @param range 全闭区间
     */
    fun createRangePool(item: ItemLike, range: FloatRange): LootPool.Builder {
        val entry = LootItem.lootTableItem(item)
        return LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1.0F))
            .apply(SetItemCountFunction.setCount(UniformGenerator.between(range.first, range.last)))
            .add(entry)
    }

    /**
     * @param range 全闭区间
     */
    fun dropOther(block: Block, item: ItemLike, range: FloatRange) {
        val pool = createRangePool(item, range)
        val table = LootTable.lootTable().withPool(pool)
        add(block, table)
    }

}