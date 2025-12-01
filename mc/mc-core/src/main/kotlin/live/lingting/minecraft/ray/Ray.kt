package live.lingting.minecraft.ray

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Function
import java.util.function.Predicate
import kotlin.math.max

/**
 * @author lingting 2025/12/2 0:09
 */
class Ray(
    val start: Vec3,
    val end: Vec3,
    radius: Double = DEFAULT_RADIUS,
    val step: Double = DEFAULT_STEP,
) {

    companion object {

        /**
         * 默认射线半径
         */
        const val DEFAULT_RADIUS = 0.3

        /**
         * 默认方块扫描步长
         */
        const val DEFAULT_STEP = 0.1

        @JvmField
        val DEFAULT_FILTER_ENTITY = Predicate<RayHitEntity> { EntitySelector.NO_SPECTATORS.test(it.entity) }

        @JvmField
        val DEFAULT_FILTER_BLOCK = Predicate<RayHitBlock> { !it.isAir() }

        @JvmField
        val DEFAULT_FILTER_HIT = Predicate<RayHit> {
            when (it) {
                is RayHitEntity -> DEFAULT_FILTER_ENTITY.test(it)
                is RayHitBlock -> DEFAULT_FILTER_BLOCK.test(it)
                else -> true
            }
        }

    }

    /**
     * 至少有个0.1的半径, 不然没法算
     */
    val radius = max(radius, 0.1)

    val aabb: AABB = AABB(
        minOf(start.x, end.x),
        minOf(start.y, end.y),
        minOf(start.z, end.z),
        maxOf(start.x, end.x),
        maxOf(start.y, end.y),
        maxOf(start.z, end.z)
    ).inflate(radius)

    private fun <S, E : RayHit> map(
        iterable: Iterable<S>,
        number: Int,
        filter: Predicate<E>,
        func: Function<S, E>,
    ): List<E> {
        if (number == 0) {
            return emptyList()
        }
        val source = mutableListOf<S>()
        val result = mutableSetOf<E>()
        for (s in iterable) {
            // 源先去重
            if (!source.add(s)) {
                continue
            }
            val e = func.apply(s)
            if (filter.test(e)) {
                result.add(e)
            }
        }
        val sortedBy = result.sortedBy { it.distanceToSqr(start) }
        if (number < 0) {
            return sortedBy
        }
        return sortedBy.take(number)
    }

    @JvmOverloads
    fun entities(
        player: Player,
        number: Int = -1,
        filter: Predicate<RayHitEntity> = DEFAULT_FILTER_ENTITY
    ): List<RayHitEntity> {
        val level = player.level()
        val entities = level.getEntities(player, aabb)
        return map(entities, number, filter) { RayHitEntity.from(it) }
    }

    @JvmOverloads
    fun blocksByStep(
        level: Level,
        number: Int = -1,
        filter: Predicate<RayHitBlock> = DEFAULT_FILTER_BLOCK
    ): List<RayHitBlock> {
        val set = mutableSetOf<BlockPos>()
        // 1. 计算方向向量和距离
        val diff = end.subtract(start)
        val distance = diff.length()
        val direction = diff.normalize()

        // 2. 步进算法收集方块
        var currentDist = 0.0
        while (currentDist <= distance) {
            // 当前检测点
            val point = start.add(direction.scale(currentDist))

            // 获取该点半径范围内的 BlockPos 范围
            val minX = (point.x - radius).toInt()
            val minY = (point.y - radius).toInt()
            val minZ = (point.z - radius).toInt()
            val maxX = (point.x + radius).toInt()
            val maxY = (point.y + radius).toInt()
            val maxZ = (point.z + radius).toInt()

            // 遍历局部范围内的方块
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    for (z in minZ..maxZ) {
                        val pos = BlockPos(x, y, z)
                        // 简单的球体/圆柱体碰撞检测：检查方块中心到射线的距离，或者简单地检查是否在半径覆盖的 BlockPos 内
                        // 为了性能，这里我们简单收集所有触及的方块，交给 map 去重
                        set.add(pos)
                    }
                }
            }

            currentDist += step
        }

        // 3. 映射并返回结果
        return map(set, number, filter) { pos ->
            RayHitBlock.from(level, pos)
        }
    }

}