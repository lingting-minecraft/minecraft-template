package live.lingting.minecraft.item

import live.lingting.minecraft.data.WeaponsData
import live.lingting.minecraft.eunums.AttackResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.component.ItemAttributeModifiers

/**
 * 远程武器
 * 远程武器自身不能进行攻击,
 * @author lingting 2025/11/29 17:17
 */
abstract class IRangedWeapons : IWeaponsItem {

    constructor(data: WeaponsData, modifiers: ItemAttributeModifiers, p: Properties) : super(data, modifiers, p)

    /**
     * @param durability 基础耐久, 为空或小于1 无线耐久
     * @see live.lingting.minecraft.util.WeaponsUtils.speedFromInterval
     */
    constructor(cls: Class<out IRangedWeapons>, data: WeaponsData, durability: Int?, p: Properties) : super(
        cls,
        data,
        durability,
        p
    )

    constructor(cls: Class<out IRangedWeapons>, tier: Tier, p: Properties) : super(cls, tier, p)

    /**
     * 自己不能攻击, 没伤害不扣耐久
     */
    override fun onLeftClickEntity(
        player: Player,
        stack: ItemStack,
        target: Entity
    ): AttackResult {
        return AttackResult.REJECT
    }

    /**
     * 自己不能攻击, 没伤害不扣耐久
     */
    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        return false
    }

}