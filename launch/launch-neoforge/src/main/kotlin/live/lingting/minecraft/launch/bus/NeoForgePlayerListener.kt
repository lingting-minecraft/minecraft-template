package live.lingting.minecraft.launch.bus

import live.lingting.minecraft.App
import live.lingting.minecraft.eunums.AttackResult
import live.lingting.minecraft.listener.BasicListener
import live.lingting.minecraft.listener.ClickListener
import live.lingting.minecraft.listener.PlayerListener
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import java.util.function.Function

/**
 * @author lingting 2025/12/1 10:38
 */
object NeoForgePlayerListener {

    @SubscribeEvent
    fun onTrigger(e: AttackEntityEvent) {
        val player = e.entity
        val target = e.target

        val playerListeners = App.listenerData.findPlayer(PlayerListener::class, player)
        var r = onTrigger(playerListeners) { it.onAttackEntity(player, target) }
        if (r == AttackResult.PASS) {
            val stack = player.mainHandItem
            val clickListeners = App.listenerData.findItem(ClickListener::class, stack)
            r = onTrigger(clickListeners) { it.onLeftClickEntity(player, stack, target) }
        }

        when (r) {
            AttackResult.REJECT -> {
                e.isCanceled = true
            }

            else -> {}
        }
    }


    @JvmStatic
    fun <T> onTrigger(listeners: List<T>, func: Function<T, AttackResult>): AttackResult {
        return BasicListener.onTrigger(listeners, func, AttackResult.PASS)
    }

}