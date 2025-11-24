package live.lingting.minecraft.launch.bus

import live.lingting.framework.util.ClassUtils
import live.lingting.minecraft.command.BasicCommand
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent

/**
 * @author lingting 2025/11/24 17:39
 */
class NeoForgeCommand(
    val classes: List<Class<out BasicCommand>>
) {

    @SubscribeEvent
    fun onCommand(e: RegisterCommandsEvent) {
        classes.forEach {
            val command = ClassUtils.newInstance(it)
            e.dispatcher.register(command.generate())
        }
    }

}