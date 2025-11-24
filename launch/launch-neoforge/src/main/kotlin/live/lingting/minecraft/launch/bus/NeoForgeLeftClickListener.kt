package live.lingting.minecraft.launch.bus

import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.eunums.ClickBlockResult
import live.lingting.minecraft.eunums.ClickBlockResult.ATTACK
import live.lingting.minecraft.eunums.ClickBlockResult.INTERACT
import live.lingting.minecraft.eunums.ClickBlockResult.REJECT
import live.lingting.minecraft.eunums.ClickBlockResult.REMOVE
import live.lingting.minecraft.listener.LeftClickListener
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.util.TriState
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

/**
 * @author lingting 2025/11/21 14:36
 */
object NeoForgeLeftClickListener {

    /**
     * 类似的事件 mc可能会因为网络和内部重试(特别是在返回REJECT时)导致一个点击多次触发相同的事件. 内部实现要做好防重.
     */
    @SubscribeEvent
    fun onTrigger(e: PlayerInteractEvent.LeftClickBlock) {
        val player = e.entity

        var hand = InteractionHand.MAIN_HAND
        var r = onTrigger(e, player.getItemInHand(hand))
        if (r == ClickBlockResult.PASS) {
            hand = InteractionHand.OFF_HAND
            r = onTrigger(e, player.getItemInHand(hand))
        }
        when (r) {
            REMOVE -> {
                val level = player.level()
                level.destroyBlock(e.pos, true, player)
                e.isCanceled = true
            }

            INTERACT -> {
                e.useBlock = TriState.TRUE
                e.useItem = TriState.TRUE
                e.isCanceled = true
            }

            ATTACK -> {
                // 仅在使用主手物品攻击时生效
                if (hand == InteractionHand.MAIN_HAND) {
                    e.useBlock = TriState.FALSE
                    e.useItem = TriState.FALSE
                }
            }

            REJECT -> {
                e.useBlock = TriState.FALSE
                e.useItem = TriState.FALSE
                e.isCanceled = true
            }

            else -> {}
        }
    }

    fun onTrigger(e: PlayerInteractEvent.LeftClickBlock, stack: ItemStack): ClickBlockResult {
        val action = e.action

        when (action) {
            PlayerInteractEvent.LeftClickBlock.Action.START -> {
                if (stack.item.isSuper(LeftClickListener::class)) {
                    return (stack.item as LeftClickListener).onLeftClickBlock(e.entity, stack, e.pos)
                }
            }

            else -> {}
        }

        return ClickBlockResult.PASS
    }

}