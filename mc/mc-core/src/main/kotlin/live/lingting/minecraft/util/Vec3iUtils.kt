package live.lingting.minecraft.util

import net.minecraft.core.SectionPos
import net.minecraft.core.Vec3i

/**
 * @author lingting 2025/11/4 19:23
 */
object Vec3iUtils {

    inline val Vec3i.chunkX: Int get() = SectionPos.blockToSectionCoord(x)

    inline val Vec3i.chunkZ: Int get() = SectionPos.blockToSectionCoord(z)

    @JvmStatic
    fun Vec3i.yRange(radius: Int, side: Boolean = false, self: Boolean = false): Sequence<Vec3i> {
        if (radius < 1) {
            if (!self || radius < 0) {
                return emptySequence()
            }
            return sequenceOf(this)
        }

        val minX = x - radius
        val maxX = x + radius
        val minZ = z - radius
        val maxZ = z + radius

        // 实心
        if (!side) {
            return sequence {
                if (self) yield(this@yRange)

                for (cx in minX..maxX) {
                    for (cz in minZ..maxZ) {
                        yield(Vec3i(cx, y, cz))
                    }
                }
            }
        }

        // 空心
        return sequence {
            if (self) yield(this@yRange)

            // 上边 (z = minZ, x 从 minX 到 maxX)
            for (cx in minX..maxX) yield(Vec3i(cx, y, minZ))

            // 下边 (z = maxZ, x 从 minX 到 maxX)
            for (cx in minX..maxX) yield(Vec3i(cx, y, maxZ))

            // 左边 (x = minX, z 从 minZ+1 到 maxZ-1) → 避免重复角点
            for (cz in minZ + 1..maxZ - 1) yield(Vec3i(minX, y, cz))

            // 右边 (x = maxX, z 从 minZ+1 到 maxZ-1) → 避免重复角点
            for (cz in minZ + 1..maxZ - 1) yield(Vec3i(maxX, y, cz))
        }
    }

}