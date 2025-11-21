package live.lingting.minecraft.data

import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.world.IWorld
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.function.Supplier

/**
 * @author lingting 2025/11/21 18:02
 */
interface BasicDataProvider {

    val items: List<Item>

    fun setItems(value: Supplier<List<Item>>)

    val blocks: List<Block>

    fun setBlocks(value: Supplier<List<Block>>)

    fun findItem(id: String?): Item? {
        if (id.isNullOrBlank()) {
            return null
        }
        return items.firstOrNull {
            if (it.isSuper(IWorld::class)) {
                (it as IWorld).id == id
            } else {
                false
            }
        }
    }

    fun getItem(id: String): Item = findItem(id)!!

    fun findBlock(id: String?): Block? {
        if (id.isNullOrBlank()) {
            return null
        }
        return blocks.firstOrNull {
            if (it.isSuper(IWorld::class)) {
                (it as IWorld).id == id
            } else {
                false
            }
        }
    }

    fun getBlock(id: String): Block = findBlock(id)!!

}