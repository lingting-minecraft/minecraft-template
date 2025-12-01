package live.lingting.minecraft.ray

import live.lingting.minecraft.util.LevelUtils.getBlockState
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

/**
 * @author lingting 2025/12/1 16:14
 */
abstract class RayHitBlock : RayHit() {

    companion object {

        @JvmStatic
        fun from(pos: Vec3i, state: BlockState): RayHitBlock {
            return StateRayHitBlock(pos, state)
        }

        @JvmStatic
        fun from(level: Level, pos: Vec3i): RayHitBlock {
            return PosRayHitBlock(level, pos)
        }

    }

    override val blockFlag: Boolean = true

    override val airFlag: Boolean
        get() = state.isAir

    override val entityFlag: Boolean = false

    override val pos: Vec3 by lazy { Vec3.atCenterOf(blockPos) }

    override fun equals(other: Any?): Boolean {
        if (other !is RayHitBlock) return false
        return blockPos == other.blockPos
    }

    override fun hashCode(): Int {
        return blockPos.hashCode()
    }

    abstract val blockPos: Vec3i

    abstract val state: BlockState

    private class StateRayHitBlock(
        override val blockPos: Vec3i,
        override val state: BlockState,
    ) : RayHitBlock()

    private class PosRayHitBlock(private val level: Level, override val blockPos: Vec3i) : RayHitBlock() {

        override val state: BlockState by lazy { level.getBlockState(blockPos) }

    }

}