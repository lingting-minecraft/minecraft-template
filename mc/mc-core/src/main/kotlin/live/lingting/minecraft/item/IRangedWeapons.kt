package live.lingting.minecraft.item

import live.lingting.minecraft.data.WeaponsData
import live.lingting.minecraft.eunums.AttackResult
import live.lingting.minecraft.ray.RayHit
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.Level

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
     * 顺序 返回 所有有效命中实体
     */
    abstract fun hitEntities(
        level: Level,
        player: Player,
        hand: InteractionHand,
        stack: ItemStack,
        data: WeaponsData
    ): List<RayHit>

    /**
     * 顺序攻击所有实体
     */
    abstract fun attackEntities(
        level: Level,
        player: Player,
        hand: InteractionHand,
        stack: ItemStack,
        data: WeaponsData,
        hits: List<RayHit>
    )

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

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        val durability = getDurability(stack, null, player)
        if (durability < 1) {
            return InteractionResultHolder.pass(stack)
        }
        val data = stack.get(WeaponsData.TYPE)
        if (data == null) return InteractionResultHolder.pass(stack)
        // 冷却
        if (player.cooldowns.isOnCooldown(stack.item)) {
            return InteractionResultHolder.pass(stack)
        }
        val entities = hitEntities(level, player, hand, stack, data)
        attackEntities(level, player, hand, stack, data, entities)
        return InteractionResultHolder.success(stack)
    }

}