package live.lingting.minecraft.ray

import live.lingting.minecraft.util.Vec3Utils.distanceToSqr
import live.lingting.minecraft.util.Vec3Utils.string
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

/**
 * @author lingting 2025/12/1 16:12
 */
abstract class RayHit {

    companion object {

        @JvmStatic
        fun from(pos: BlockPos, state: BlockState): RayHitBlock {
            return RayHitBlock.from(pos, state)
        }

        @JvmStatic
        fun from(level: Level, pos: BlockPos): RayHitBlock {
            return RayHitBlock.from(level, pos)
        }

        @JvmStatic
        fun from(entity: Entity): RayHitEntity {
            return RayHitEntity.from(entity)
        }

    }

    protected abstract val blockFlag: Boolean

    protected abstract val airFlag: Boolean

    protected abstract val entityFlag: Boolean

    abstract val pos: Vec3

    fun isBlock(): Boolean {
        return blockFlag
    }

    fun isAir(): Boolean {
        return blockFlag && airFlag
    }

    fun isEntity(): Boolean {
        return entityFlag
    }

    fun distanceToSqr(s: Entity): Double = distanceToSqr(s.position())

    fun distanceToSqr(vec: Vec3i): Double {
        return pos.distanceToSqr(vec)
    }

    fun distanceToSqr(vec: Vec3): Double {
        return pos.distanceToSqr(vec)
    }

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    override fun toString(): String {
        return "[${javaClass.simpleName}] ${pos.string()}"
    }

}