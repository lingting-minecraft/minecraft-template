package live.lingting.minecraft.launch.bus

import live.lingting.minecraft.App
import live.lingting.minecraft.eunums.ClickBlockResult
import live.lingting.minecraft.eunums.ClickBlockResult.ATTACK
import live.lingting.minecraft.eunums.ClickBlockResult.INTERACT
import live.lingting.minecraft.eunums.ClickBlockResult.REJECT
import live.lingting.minecraft.eunums.ClickBlockResult.REMOVE
import live.lingting.minecraft.listener.BasicListener
import live.lingting.minecraft.listener.ClickListener
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.util.TriState
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import java.util.function.Function

/**
 * @author lingting 2025/11/21 14:36
 */
object NeoForgeClickListener {

    /**
     * 类似的事件 mc可能会因为网络和内部重试(特别是在返回REJECT时)导致一个点击多次触发相同的事件. 内部实现要做好防重.
     */
    @SubscribeEvent
    fun onTrigger(e: PlayerInteractEvent.LeftClickBlock) {
        val player = e.entity

        val r = onTrigger(e, player.mainHandItem)
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
                e.useBlock = TriState.FALSE
                e.useItem = TriState.FALSE
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
        val listeners = App.listenerData.findItem(ClickListener::class, stack)
        return onTrigger(listeners) {
            when (action) {
                PlayerInteractEvent.LeftClickBlock.Action.START -> it.onLeftClickBlock(e.entity, stack, e.pos)
                else -> ClickBlockResult.PASS
            }
        }
    }

    fun <T> onTrigger(listeners: List<T>, func: Function<T, ClickBlockResult>): ClickBlockResult {
        return BasicListener.onTrigger(listeners, func, ClickBlockResult.PASS)
    }

}