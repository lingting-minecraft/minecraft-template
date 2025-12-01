package live.lingting.minecraft.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.ray.RayHit
import live.lingting.minecraft.util.RayUtils
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Tier
import net.minecraft.world.phys.Vec3
import java.util.function.Supplier

/**
 * @author lingting 2025/11/29 23:38
 */
data class WeaponsData(
    /**
     * 攻击伤害
     */
    val damage: Double,
    /**
     * 攻击间隔, 单位: 秒
     */
    val speed: Double,
    /**
     * 攻击距离
     */
    val distance: Double = 3.0,
    /**
     * 伤害范围
     * 0: 只对命中目标有伤害. 具体看不同武器的实现
     */
    val range: Double = 0.0,
    /**
     * 最终伤害每格衰减多少. 具体值
     * eg: damageDistanceDecay=0.1  原始伤害: 10 距离: 2格 -> 实际伤害: 9.8
     */
    val damageDistanceDecay: Double = 0.0,
    /**
     * 最大可穿透实体数量, 小于0则数量无限
     */
    val penetrationNumber: Int = 0,
    /**
     * 穿透一个实体后, 伤害衰减多少. 具体值
     * eg: damagePenetrationDecay=0.1  原始伤害: 10 穿透实体: 1 -> 穿透实体伤害: 10, 下一个实体伤害: 9.9
     */
    val damagePenetrationDecay: Double = 0.0,
) : BasicComponentData() {

    companion object {

        @JvmField
        val CODEC: Codec<WeaponsData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("damage").forGetter { it.damage },
                Codec.DOUBLE.fieldOf("speed").forGetter { it.speed },
                Codec.DOUBLE.fieldOf("distance").forGetter { it.distance },
                Codec.DOUBLE.fieldOf("range").forGetter { it.range },
                Codec.DOUBLE.fieldOf("damageDistanceDecay").forGetter { it.damageDistanceDecay },
                Codec.INT.fieldOf("penetrationNumber").forGetter { it.penetrationNumber },
                Codec.DOUBLE.fieldOf("damagePenetrationDecay").forGetter { it.damagePenetrationDecay },
            )
                .apply(instance) { damage, speed, distance, range, damageDistanceDecay, penetrationNumber, damagePenetrationDecay ->
                    WeaponsData(
                        damage,
                        speed,
                        distance,
                        range,
                        damageDistanceDecay,
                        penetrationNumber,
                        damagePenetrationDecay
                    )
                }
        }

        private val TYPE_VALUE = WaitValue.of<Supplier<DataComponentType<WeaponsData>>>()

        val TYPE: DataComponentType<WeaponsData>
            get() = TYPE_VALUE.notNull().get()

        @JvmStatic
        fun setType(supplier: Supplier<DataComponentType<WeaponsData>>) {
            TYPE_VALUE.update(supplier)
        }

        @JvmStatic
        fun fromTier(tier: Tier): WeaponsData {
            return WeaponsData(tier.attackDamageBonus.toDouble(), tier.speed.toDouble())
        }

    }

    /**
     * 攻击间隔, 单位: tick
     */
    val speedTick = (speed * 20).toInt()

    /**
     * 最大命中数量
     * 小于0表示无限, 大于等于0表示具体的穿透数量, 还需要加上默认的1
     */
    val hitNumber = if (penetrationNumber < 0) -1 else penetrationNumber + 1

    /**
     * 计算射线起点和终点
     */
    fun rayPos(player: Player, entities: List<Entity>): Pair<Vec3, Vec3> {
        val last = entities.lastOrNull()
        if (last != null) {
            return player.eyePosition to last.position()
        }

        return RayUtils.pos(player, distance)
    }

    /**
     * 伤害计算, 计算对所有实体造成的伤害
     * @param basic 基础伤害
     */
    fun damage(start: Vec3, basic: Double, hits: List<RayHit>): Map<RayHit, Double?> {
        val map = mutableMapOf<RayHit, Double?>()
        var damageStart = start
        var damage = basic

        hits.forEachIndexed { i, hit ->
            val distance = hit.distanceToSqr(damageStart)
            // 距离衰减
            val distanceDamage = damage - distance * damageDistanceDecay
            // 对hit造成伤害
            map[hit] = distanceDamage
            // 下一个实体计算距离衰减伤害起点
            damageStart = hit.pos
            // 下一个实体的基础伤害 = 当前伤害 - 穿透帅衰减
            damage = distanceDamage - damagePenetrationDecay
        }


        return map
    }

}