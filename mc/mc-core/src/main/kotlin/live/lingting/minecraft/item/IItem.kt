package live.lingting.minecraft.item

import live.lingting.minecraft.world.IWorld
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

/**
 * @author lingting 2025/11/6 19:23
 */
abstract class IItem : Item, ItemSource, IWorld {

    constructor(p: Properties) : super(p)

    /**
     * 修改物品耐久值。 修改后无耐久也不会破损
     * @param stack 要修改的物品
     * @param value 耐久变化量：
     *   - **正数**：修复耐久（减少已损耗值）
     *   - **负数**：消耗耐久（增加已损耗值）
     * @param player 可选的实体，用于触发进度（仅当是 ServerPlayer 时生效）
     */
    @JvmOverloads
    open fun durabilityChange(stack: ItemStack, value: Int, player: Entity? = null) {
        if (value == 0) {
            return
        }
        // 最大耐久
        val max = stack.maxDamage
        if (max < 1) {
            return
        }
        // 新的已使用耐久 = 当前已使用耐久 - 变化值.
        val used = stack.damageValue - value
        // 已使用耐久必须在 (0,max) 全闭区间 内
        val coerce = used.coerceIn(0, max)
        stack.damageValue = coerce
        // 触发成就进度通知
        if (player is ServerPlayer) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(player, stack, coerce)
        }
    }

    // region mc

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        return super.use(level, player, hand)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        return super.useOn(context)
    }

    // endregion

}