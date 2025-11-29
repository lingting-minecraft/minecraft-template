package live.lingting.minecraft.item

import live.lingting.minecraft.util.ItemStackUtils.getEnchantmentLevel
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.enchantment.Enchantments

/**
 * @author lingting 2025/11/29 15:07
 */
abstract class IMeleeWeaponsItem : IWeaponsItem {

    constructor(p: Properties, modifiers: ItemAttributeModifiers) : super(p, modifiers)

    /**
     * @param damage 基础伤害
     * @param speed 基础攻击
     * @param durability 基础耐久, 为空或小于1 无线耐久
     * @see live.lingting.minecraft.util.WeaponsUtils.speedFromInterval
     */
    constructor(cls: Class<out IWeaponsItem>, damage: Double, speed: Double, durability: Int?, p: Properties)
            : super(cls, damage, speed, durability, p)

    constructor(cls: Class<out IWeaponsItem>, tier: Tier, p: Properties) : super(cls, tier, p)

    /**
     * 近战武器基础伤害吃力量附魔
     */
    override fun getBasicDamage(stack: ItemStack, holder: HolderLookup.Provider?): Double {
        val basic = super.getBasicDamage(stack, holder)
        val i = stack.getEnchantmentLevel(holder, Enchantments.POWER) ?: 0
        return basic + i
    }

}