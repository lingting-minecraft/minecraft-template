package live.lingting.minecraft.launch.model

import jakarta.annotation.Resource
import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.world.IWorld

/**
 * @author lingting 2025/10/18 16:46
 */
abstract class NModel {


    private val sourceValue = WaitValue.of<IWorld>()
    var source
        get() = sourceValue.notNull()
        @Resource
        set(value) = sourceValue.update(value)

    val id
        get() = source.id

    abstract val types: Collection<Class<out IWorld>>

    abstract fun register()

}