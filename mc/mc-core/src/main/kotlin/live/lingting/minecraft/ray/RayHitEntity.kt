package live.lingting.minecraft.ray

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

/**
 * @author lingting 2025/12/1 16:14
 */
class RayHitEntity(val entity: Entity) : RayHit() {

    companion object {

        @JvmStatic
        fun from(entity: Entity): RayHitEntity {
            return RayHitEntity(entity)
        }

    }

    override val blockFlag: Boolean = false

    override val airFlag: Boolean = false

    override val entityFlag: Boolean = true

    override val pos: Vec3 = entity.position()

    override fun equals(other: Any?): Boolean {
        if (other !is RayHitEntity) return false
        return entity == other.entity
    }

    override fun hashCode(): Int {
        return entity.hashCode()
    }

}