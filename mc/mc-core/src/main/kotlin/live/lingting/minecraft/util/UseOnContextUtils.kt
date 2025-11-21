package live.lingting.minecraft.util

import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

/**
 * @author lingting 2025/11/21 13:56
 */
object UseOnContextUtils {

    @JvmStatic
    val UseOnContext.isClientSide get() = level.isClientSide

    @JvmStatic
    fun UseOnContext.targetState(): BlockState = level.getBlockState(clickedPos)

    @JvmStatic
    fun UseOnContext.targetEntity(): BlockEntity? = level.getBlockEntity(clickedPos)

}