package live.lingting.minecraft.loot

import live.lingting.framework.value.WaitValue
import net.minecraft.core.HolderLookup
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.function.Supplier

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

    private val itemsValue = WaitValue.of<Supplier<List<Item>>>()

    override val items
        get() = itemsValue.notNull().get()

    override fun setItems(value: Supplier<List<Item>>) = itemsValue.update(value)

    private val blocksValue = WaitValue.of<Supplier<List<Block>>>()

    override val blocks
        get() = blocksValue.notNull().get()

    override fun setBlocks(value: Supplier<List<Block>>) = blocksValue.update(value)

    /**
     * 重写 neoforge 自己新增的方法, 返回模组的物品
     */
    protected fun getKnownBlocks(): Iterable<Block> {
        return blocks
    }

}