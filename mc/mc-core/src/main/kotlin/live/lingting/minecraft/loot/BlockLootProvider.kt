package live.lingting.minecraft.loot

import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.data.RegisterData
import live.lingting.minecraft.kt.number
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
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

    protected val enchantment: HolderLookup.RegistryLookup<Enchantment>
        get() = registries.lookupOrThrow(Registries.ENCHANTMENT)

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
        cache[block] = builder
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
        return createCountPool(item, 1.number)
    }

    fun createCountPool(item: ItemLike, number: NumberProvider): LootPool.Builder {
        val entry = LootItem.lootTableItem(item)
            .apply(SetItemCountFunction.setCount(number))
        return LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1.0F))
            .add(entry)
    }

    fun dropOther(block: Block, item: ItemLike, number: NumberProvider) {
        val pool = createCountPool(item, number)
        val table = LootTable.lootTable().withPool(pool)
        add(block, table)
    }

    /**
     * 普通掉落物
     * 精准采集掉落完全复制的自己
     * 非精准采集掉落随机数量的其他物品
     */
    fun dropNormal(block: Block, item: ItemLike, number: NumberProvider) {
        val table = LootTable.lootTable()
            // 精准采集 - 获取一个自己
            .withPool(
                createSinglePool(block)
                    .`when`(hasSilkTouch())
            )
            // 其他工具 - 获取掉落物
            .withPool(
                createCountPool(item, number)
                    .`when`(doesNotHaveSilkTouch())
            )
        add(block, table)
    }

    /**
     * @param number 基础掉落数量
     * @param magnification 基础倍率, 每级时运增长掉落物数量提高倍数
     */
    @JvmOverloads
    fun createFortunePool(item: ItemLike, number: NumberProvider, magnification: Int = 1): LootPool.Builder {
        val reference = enchantment.getOrThrow(Enchantments.FORTUNE)

        val entry = LootItem.lootTableItem(item)
            .apply(SetItemCountFunction.setCount(number))
            .apply(ApplyBonusCount.addUniformBonusCount(reference, magnification))

        return LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1.0F))
            .add(entry)
    }

    /**
     * 普通掉落物
     * 精准采集掉落完全复制的自己
     * 非精准采集掉落 根据 时运等级掉落随机数量的其他物品
     */
    @JvmOverloads
    fun dropNormalWithFortune(block: Block, item: ItemLike, number: NumberProvider, magnification: Int = 1) {
        val table = LootTable.lootTable()
            // 精准采集 - 获取一个自己
            .withPool(
                createSinglePool(block)
                    .`when`(hasSilkTouch())
            )
            // 其他工具 - 获取掉落物
            .withPool(
                createFortunePool(item, number, magnification)
                    .`when`(doesNotHaveSilkTouch())
            )
        add(block, table)
    }

}