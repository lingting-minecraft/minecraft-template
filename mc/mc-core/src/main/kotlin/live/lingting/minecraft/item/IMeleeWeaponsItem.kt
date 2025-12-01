package live.lingting.minecraft.item

import live.lingting.minecraft.data.WeaponsData
import live.lingting.minecraft.util.ItemStackUtils.getEnchantmentLevel
import net.minecraft.core.HolderLookup
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.enchantment.Enchantments
import kotlin.math.max

/**
 * @author lingting 2025/11/29 15:07
 */
abstract class IMeleeWeaponsItem : IWeaponsItem {

    constructor(data: WeaponsData, modifiers: ItemAttributeModifiers, p: Properties) : super(data, modifiers, p)

    /**
     * @param durability 基础耐久, 为空或小于1 无线耐久
     * @see live.lingting.minecraft.util.WeaponsUtils.speedFromInterval
     */
    constructor(cls: Class<out IMeleeWeaponsItem>, data: WeaponsData, durability: Int?, p: Properties) : super(
        cls,
        data,
        durability,
        p
    )

    constructor(cls: Class<out IMeleeWeaponsItem>, tier: Tier, p: Properties) : super(cls, tier, p)

    /**
     * 近战武器基础伤害吃默认力量附魔
     */
    override fun getBasicDamage(stack: ItemStack, holder: HolderLookup.Provider?): Double {
        val basic = super.getBasicDamage(stack, holder)
        val i = stack.getEnchantmentLevel(holder, Enchantments.POWER) ?: 0
        return basic + i
    }

    /**
     * 近战武器附加伤害默认吃锋利附魔
     */
    override fun getAdditionalDamage(
        stack: ItemStack,
        basic: Double,
        holder: HolderLookup.Provider?,
        target: LivingEntity?,
        attacker: LivingEntity?
    ): Double {
        val i = stack.getEnchantmentLevel(holder, Enchantments.SHARPNESS) ?: 0
        return basic * i * 0.5
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        val level = attacker.level()
        if (level.isClientSide) return true
        // 攻击伤害, 最小值是0
        val damage = max(0.0, getDamage(stack, level, target, attacker)).toFloat()
        // 无来源时, 使用通用来源
        val source = attacker.lastDamageSource ?: level.damageSources().generic()
        val hurt = target.hurt(source, damage)
        if (hurt && damage > 0) {
            // 扣减一点耐久
            durabilityChange(stack, -1, attacker)
        }
        return true
    }

}