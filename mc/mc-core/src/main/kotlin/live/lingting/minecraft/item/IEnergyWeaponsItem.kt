package live.lingting.minecraft.item

import live.lingting.minecraft.data.WeaponsData
import live.lingting.minecraft.ray.RayHit
import live.lingting.minecraft.ray.RayHitEntity
import live.lingting.minecraft.render.LaserRenderer
import live.lingting.minecraft.util.RayUtils
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.Level

/**
 * 能量武器
 * 由于没有 发射出去的实体, 所以伤害全部加在自己身上用于计算
 * @author lingting 2025/11/29 17:15
 */
abstract class IEnergyWeaponsItem : IRangedWeapons {

    constructor(data: WeaponsData, modifiers: ItemAttributeModifiers, p: Properties) : super(data, modifiers, p)

    /**
     * @param durability 基础耐久, 为空或小于1 无线耐久
     * @see live.lingting.minecraft.util.WeaponsUtils.speedFromInterval
     */
    constructor(cls: Class<out IEnergyWeaponsItem>, data: WeaponsData, durability: Int?, p: Properties) : super(
        cls,
        data,
        durability,
        p
    )

    constructor(cls: Class<out IEnergyWeaponsItem>, tier: Tier, p: Properties) : super(cls, tier, p)

    override fun hitEntities(
        level: Level,
        player: Player,
        hand: InteractionHand,
        stack: ItemStack,
        data: WeaponsData
    ): List<RayHit> {
        return RayUtils.hits(player, data.distance, data.range, data.hitNumber)
    }

    override fun attackEntities(
        level: Level,
        player: Player,
        hand: InteractionHand,
        stack: ItemStack,
        data: WeaponsData,
        hits: List<RayHit>
    ) {
        if (level.isClientSide) {
            LaserRenderer.push(player, data, hits)
        } else {
            val map = attackDamage(level, player, stack, data, hits)
            val source = player.lastDamageSource ?: level.damageSources().generic()
            map.forEach { hit, damage ->
                if (!hit.isEntity() || damage == null) {
                    return@forEach
                }
                if (damage <= 0.0) {
                    return@forEach
                }
                val target = (hit as RayHitEntity).entity
                if (target !is LivingEntity) {
                    return@forEach
                }
                target.hurt(source, damage.toFloat())
            }
            // 能量释放就减少耐久
            durabilityChange(stack, -1, player)
            player.cooldowns.addCooldown(stack.item, data.speedTick)
        }
    }

    /**
     * 计算对所有命中者的伤害.
     * 如果有对方块的破坏功能, 也可以在这里实现.
     */
    open fun attackDamage(
        level: Level,
        player: Player,
        stack: ItemStack,
        data: WeaponsData,
        hits: List<RayHit>
    ): Map<RayHit, Double?> {
        val basic = getDamage(stack, level, null, player)
        return data.damage(player.eyePosition, basic, hits)
    }

}