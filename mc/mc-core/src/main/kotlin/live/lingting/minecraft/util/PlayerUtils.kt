package live.lingting.minecraft.util

import net.minecraft.world.entity.player.Player

/**
 * @author lingting 2025/11/21 15:17
 */
object PlayerUtils {

    @JvmStatic
    val Player.isClientSide get() = level().isClientSide

}