package live.lingting.minecraft.util

import net.minecraft.core.Vec3i
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

/**
 * @author lingting 2025/12/1 16:40
 */
object Vec3Utils {

    fun Vec3.distanceToSqr(s: Entity): Double = distanceToSqr(s.position())

    fun Vec3.distanceToSqr(vec: Vec3i): Double = distanceToSqr(Vec3.atCenterOf(vec))

    fun Vec3.distanceToSqr(vec3: Vec3): Double {
        val d: Double = x - vec3.x
        val e: Double = y - vec3.y
        val f: Double = z - vec3.z
        return d * d + e * e + f * f
    }

    fun Vec3.string() = "$x $y $z"

}