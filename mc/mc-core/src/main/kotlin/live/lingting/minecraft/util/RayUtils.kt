package live.lingting.minecraft.util

import live.lingting.minecraft.ray.Ray
import live.lingting.minecraft.ray.Ray.Companion.DEFAULT_FILTER_BLOCK
import live.lingting.minecraft.ray.Ray.Companion.DEFAULT_FILTER_ENTITY
import live.lingting.minecraft.ray.Ray.Companion.DEFAULT_FILTER_HIT
import live.lingting.minecraft.ray.Ray.Companion.DEFAULT_RADIUS
import live.lingting.minecraft.ray.RayHit
import live.lingting.minecraft.ray.RayHitBlock
import live.lingting.minecraft.ray.RayHitEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

/**
 * 射线工具
 * @author lingting 2025/12/1 15:19
 */
object RayUtils {

    /**
     * 计算射线起点和终点
     */
    @JvmStatic
    fun pos(player: Player, distance: Double): Pair<Vec3, Vec3> {
        val start = player.eyePosition
        val look = player.lookAngle
        val scale = look.scale(distance)
        val end = start.add(scale)
        return start to end
    }

    /**
     * 获取距离内的所有命中目标
     * @param radius 射线半径
     * @param number 最多多少个目标, 小于0表示无限
     */
    @JvmStatic
    @JvmOverloads
    fun hits(
        player: Player,
        distance: Double,
        radius: Double = DEFAULT_RADIUS,
        number: Int = -1,
        filter: Predicate<RayHit> = DEFAULT_FILTER_HIT
    ): List<RayHit> {
        if (number == 0) {
            return emptyList()
        }
        val level = player.level()
        val (start, end) = pos(player, distance)
        val ray = Ray(start, end, radius)
        val entities = ray.entities(player, number) { filter.test(it) }
        val blocks = ray.blocksByStep(level, number) { filter.test(it) }

        val hits = mutableSetOf<RayHit>()
        hits.addAll(entities)
        hits.addAll(blocks)

        // 从近到远排序
        val sortedBy = hits.sortedBy { it.distanceToSqr(start) }
        return if (number < 0) sortedBy else sortedBy.take(number)
    }

    /**
     * 获取距离内的所有命中实体
     * @param radius 射线半径
     * @param number 最多多少个实体, 小于0表示无限
     */
    @JvmStatic
    @JvmOverloads
    fun hitEntities(
        player: Player,
        start: Vec3,
        end: Vec3,
        radius: Double = DEFAULT_RADIUS,
        number: Int = -1,
        filter: Predicate<RayHitEntity> = DEFAULT_FILTER_ENTITY
    ): List<RayHitEntity> {
        if (number == 0) {
            return emptyList()
        }
        val ray = Ray(start, end, radius)
        return ray.entities(player, number) { filter.test(it) }
    }

    @JvmStatic
    @JvmOverloads
    fun hitEntities(
        player: Player,
        distance: Double,
        radius: Double = DEFAULT_RADIUS,
        number: Int = -1,
        filter: Predicate<RayHitEntity> = DEFAULT_FILTER_ENTITY
    ): List<RayHit> {
        val (start, end) = pos(player, distance)
        return hitEntities(player, start, end, radius, number, filter)
    }

    /**
     * 获取距离内的所有命中方块.
     * 不管碰撞箱, 只要经过格子就算命中
     *
     * 使用体素遍历 (DDA思想) 结合 radius 参数实现粗射线检测。
     * 只要射线中心线穿过方块 B0，就检查 B0 附近半径 r 范围内的所有方块。
     * @param radius 射线半径
     * @param number 最多多少个方块, 小于0表示无限
     */
    @JvmStatic
    @JvmOverloads
    fun hitBlocks(
        level: Level,
        start: Vec3,
        end: Vec3,
        radius: Double = DEFAULT_RADIUS,
        number: Int = -1,
        filter: Predicate<RayHitBlock> = DEFAULT_FILTER_BLOCK
    ): List<RayHitBlock> {
        if (number == 0) return emptyList()
        val ray = Ray(start, end, radius)
        return ray.blocksByStep(level, number, filter)
    }

}