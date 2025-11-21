package live.lingting.minecraft.util

import live.lingting.framework.util.ClassUtils
import live.lingting.minecraft.world.IWorld
import net.minecraft.world.level.block.state.BlockState
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * @author lingting 2025/11/21 13:59
 */
object BlockStateUtils {

    @JvmStatic
    @OptIn(ExperimentalContracts::class)
    fun BlockState?.`is`(id: String?): Boolean {
        contract {
            returns(true) implies (this@`is` != null && id != null)
        }
        if (this == null || id.isNullOrBlank()) {
            return false
        }
        if (!ClassUtils.isSuper(block, IWorld::class.java)) {
            return false
        }
        val i = block as IWorld
        return i.id == id
    }

}