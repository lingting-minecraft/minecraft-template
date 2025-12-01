package live.lingting.minecraft.item

import live.lingting.framework.util.DigestUtils
import live.lingting.minecraft.data.WeaponsData
import live.lingting.minecraft.eunums.AttackResult
import live.lingting.minecraft.kt.location
import live.lingting.minecraft.listener.ClickListener
import live.lingting.minecraft.util.WeaponsUtils
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.max

/**
 * 武器基类
 * @author lingting 2025/11/29 1:08
 */
abstract class IWeaponsItem : IItem, ClickListener {

    companion object {

        @JvmStatic
        fun createAttributes(cls: Class<out IWeaponsItem>, data: WeaponsData): ItemAttributeModifiers.Builder {
            val key = DigestUtils.md5Hex(cls.name)
            val builder = ItemAttributeModifiers.builder()
            builder.add(
                Attributes.ATTACK_DAMAGE,
                AttributeModifier("${key}_damage".location(), 0.0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            // 攻击间隔转为攻击速度
            val speed = WeaponsUtils.speedFromInterval(data.speed)
            // 附加攻击速度 = 最终攻击速度 - MC设定基础攻击速度(-4)
            val addSpeed = speed - -4
            return builder
                .add(
                    Attributes.ATTACK_SPEED,
                    AttributeModifier("${key}_speed".location(), addSpeed, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND
                )
        }

    }

    constructor(data: WeaponsData, modifiers: ItemAttributeModifiers, p: Properties) : super(p.also {
        it.component(DataComponents.ATTRIBUTE_MODIFIERS, modifiers)
        it.component(WeaponsData.TYPE, data)
    })

    /**
     * @param durability 基础耐久, 为空或小于1 无线耐久
     * @see live.lingting.minecraft.util.WeaponsUtils.speedFromInterval
     */
    constructor(cls: Class<out IWeaponsItem>, data: WeaponsData, durability: Int?, p: Properties) : this(
        data, createAttributes(cls, data).build(),
        p.apply {
            stacksTo(1)
            if (durability != null && durability > 0) {
                durability(durability)
            }
        }
    )

    constructor(cls: Class<out IWeaponsItem>, tier: Tier, p: Properties) : this(
        cls,
        WeaponsData.fromTier(tier),
        tier.uses,
        p
    )

    /**
     * 计算物品当前耐久
     * @param stack 物品
     * @param target 攻击目标, 可能为空. 不为空时可以根据目标状态来变更耐久
     * @param attacker 攻击者, 可能为空. 不为空时可以根据物品持有者状态来变更耐久
     */
    @JvmOverloads
    open fun getDurability(stack: ItemStack, target: LivingEntity? = null, attacker: LivingEntity? = null): Int {
        return stack.maxDamage - stack.damageValue
    }

    /**
     * 计算真实攻击伤害
     * @param stack 物品
     * @param level 持有者所在世界
     * @param target 攻击目标, 可能为空. 不为空时可以根据目标状态来变更攻击伤害
     * @param attacker 攻击者, 可能为空. 不为空时可以根据物品持有者状态来变更攻击伤害
     */
    @JvmOverloads
    open fun getDamage(
        stack: ItemStack,
        level: Level,
        target: LivingEntity? = null,
        attacker: LivingEntity? = null
    ): Double {
        return getDamage(stack, level.registryAccess(), target, attacker)
    }

    @JvmOverloads
    open fun getDamage(
        stack: ItemStack,
        holder: HolderLookup.Provider?,
        target: LivingEntity? = null,
        attacker: LivingEntity? = null
    ): Double {
        val durability = getDurability(stack, target, attacker)
        // 无耐久无法造成伤害
        if (durability < 1) {
            return 0.0
        }
        val basic = getBasicDamage(stack, holder)
        val additional = getAdditionalDamage(stack, basic, holder, target, attacker)
        return basic + additional
    }

    /**
     * 获取基础伤害
     */
    open fun getBasicDamage(stack: ItemStack, holder: HolderLookup.Provider?): Double {
        val data = stack.get(WeaponsData.TYPE)
        return data?.damage ?: 0.0
    }

    /**
     * 获取 在基础伤害基础上的 附加伤害
     */
    open fun getAdditionalDamage(
        stack: ItemStack,
        basic: Double,
        holder: HolderLookup.Provider?,
        target: LivingEntity? = null,
        attacker: LivingEntity? = null
    ): Double {
        return 0.0
    }

    // region hurt

    /**
     * 应该仅在服务端触发, 用于武器自定义攻击行为时触发
     * - 计算攻击伤害
     * - 触发攻击效果
     * - 有效攻击时扣减耐久
     */
    @JvmOverloads
    open fun onHurt(
        stack: ItemStack,
        level: Level,
        target: LivingEntity? = null,
        attacker: LivingEntity? = null
    ) {
        if (level.isClientSide) return
        // 攻击伤害, 最小值是0
        val damage = max(0.0, getDamage(stack, level, target, attacker)).toFloat()
        // 无来源时, 使用通用来源
        val source = attacker?.lastDamageSource ?: level.damageSources().generic()
        val hurt = target?.hurt(source, damage) ?: false
        log.debug("当前血量: {}; 当前伤害: {}", target?.health, damage)
        onHurtAfter(stack, level, hurt, damage, target, attacker)
    }

    /**
     * 攻击完后触发
     * @param hurt 是否攻击到目标
     * @param damage 造成的伤害
     */
    open fun onHurtAfter(
        stack: ItemStack,
        level: Level,
        hurt: Boolean,
        damage: Float,
        target: LivingEntity? = null,
        attacker: LivingEntity? = null
    ) {
        // 仅在造成有效伤害时扣减耐久
        if (hurt && damage > 0) {
            // 扣减一点耐久
            durabilityChange(stack, -1, attacker)
        }
    }

    override fun onLeftClickEntity(player: Player, stack: ItemStack, target: Entity): AttackResult {
        val durability = getDurability(stack, target as? LivingEntity, player)
        if (durability > 0) {
            return AttackResult.PASS
        }
        return AttackResult.REJECT
    }

    // endregion

    // region mc

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        val level = attacker.level()
        onHurt(stack, level, target, attacker)
        return true
    }

    override fun canAttackBlock(state: BlockState, level: Level, pos: BlockPos, player: Player): Boolean {
        return false
    }

    // endregion

}