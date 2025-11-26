package live.lingting.minecraft.data

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

/**
 * @author lingting 2025/11/21 18:02
 */
interface BasicDataProvider {

    var registerData: RegisterData

    val items: List<Item>
        get() = registerData.items

    val blocks: List<Block>
        get() = registerData.blocks

    fun findItem(id: String?) = registerData.findItem(id)

    fun getItem(id: String) = registerData.getItem(id)

    fun findBlock(id: String?) = registerData.findBlock(id)

    fun getBlock(id: String) = registerData.getBlock(id)

}