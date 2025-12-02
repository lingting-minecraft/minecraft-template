package live.lingting.minecraft.launch.bus

import live.lingting.minecraft.App
import live.lingting.minecraft.listener.BasicListener
import live.lingting.minecraft.listener.ClientLevelListener
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RenderLevelStageEvent
import java.util.function.Function
import kotlin.reflect.KClass

/**
 * @author lingting 2025/12/2 11:20
 */
object NeoForgeClientLevelListener {

    @SubscribeEvent
    fun onTrigger(e: ClientTickEvent.Pre) {
        onTrigger(ClientLevelListener::class) { it.onClientPreTick() }
    }

    @SubscribeEvent
    fun onTrigger(e: ClientTickEvent.Post) {
        onTrigger(ClientLevelListener::class) { it.onClientPostTick() }
    }

    @SubscribeEvent
    fun onTrigger(e: RenderLevelStageEvent) {
        val data = ClientLevelListener.RenderLevelData(
            e.levelRenderer,
            e.poseStack,
            e.modelViewMatrix,
            e.projectionMatrix,
            e.renderTick,
            e.partialTick,
            e.camera,
            e.frustum
        )

        onTrigger(ClientLevelListener::class) {
            when (e.stage) {
                RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS -> {
                    it.onRenderLevelAfterTranslucentBlocks(data)
                }
            }
        }
    }

    fun <T : BasicListener> onTrigger(cls: KClass<T>, func: Function<T, Unit?>) {
        val listeners = App.listenerData.findNone(cls)
        BasicListener.onTrigger(listeners, func, null)
    }

}