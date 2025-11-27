package live.lingting.minecraft.data

import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.world.IWorld
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.function.Supplier

/**
 * @author lingting 2025/11/23 22:54
 */
class RegisterData(
    si: Supplier<List<Item>>,
    sb: Supplier<List<Block>>
) {

    companion object {

        @JvmStatic
        fun from(si: List<Supplier<Item>>, sb: List<Supplier<Block>>): RegisterData {
            return RegisterData({ si.map { it.get() } }, { sb.map { it.get() } })
        }

    }

    val items: List<Item> by lazy { si.get() }

    val blocks: List<Block> by lazy { sb.get() }

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

    val blockSource by lazy {
        blocks.mapNotNull {
            if (it.isSuper(BlockSource::class)) {
                it as BlockSource
            } else {
                null
            }
        }
    }

}
