package live.lingting.minecraft.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import live.lingting.framework.value.WaitValue
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.Tier
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
    val range: Double = 3.0,
    /**
     * 最终伤害每格衰减多少. 具体值
     * eg: damageDistanceDecay=0.1  原始伤害: 10 距离: 2格 -> 实际伤害: 9.8
     */
    val damageDistanceDecay: Double = 0.0,
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
                Codec.DOUBLE.fieldOf("range").forGetter { it.range },
                Codec.DOUBLE.fieldOf("damageDistanceDecay").forGetter { it.damageDistanceDecay },
                Codec.DOUBLE.fieldOf("damagePenetrationDecay").forGetter { it.damagePenetrationDecay },
            ).apply(instance) { damage, speed, range, damageDistanceDecay, damagePenetrationDecay ->
                WeaponsData(damage, speed, range, damageDistanceDecay, damagePenetrationDecay)
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

}