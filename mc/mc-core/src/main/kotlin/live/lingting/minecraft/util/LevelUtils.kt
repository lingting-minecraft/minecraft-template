package live.lingting.minecraft.util

import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.util.Vec3iUtils.chunkX
import live.lingting.minecraft.util.Vec3iUtils.chunkZ
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

/**
 * @author lingting 2025/11/6 19:49
 */
object LevelUtils {

    @JvmStatic
    fun Level.hasChunk(vec: Vec3i) = hasChunk(vec.chunkX, vec.chunkZ)

    @JvmStatic
    fun Level.getBlockState(vec: Vec3i): BlockState {
        val pos = if (vec.isSuper(BlockPos::class)) {
            vec as BlockPos
        } else {
            BlockPos(vec)
        }
        return getBlockState(pos)
    }

}