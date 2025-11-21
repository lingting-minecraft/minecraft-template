package live.lingting.minecraft.loot

import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.function.Supplier

/**
 * @author lingting 2025/11/21 17:07
 */
interface BasicLootProvider : LootTableSubProvider {

    val items: List<Item>

    fun setItems(value: Supplier<List<Item>>)

    val blocks: List<Block>

    fun setBlocks(value: Supplier<List<Block>>)

}