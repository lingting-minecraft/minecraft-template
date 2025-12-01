package live.lingting.minecraft.data

import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.kt.location
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey

/**
 * @author lingting 2025/11/23 22:44
 */
abstract class BasicFeatureProvider<T> : BasicDataProvider {

    private val _context = WaitValue.of<BootstrapContext<T>>()

    var context
        get() = _context.notNull()
        set(value) = _context.update(value)

    abstract val keyPrefix: String

    /**
     * 当前配置id, 存在多个相同类型配置时, id不要重复
     */
    abstract val id: String

    val location by lazy { "${keyPrefix}.${id}".location() }

    val key: ResourceKey<T> by lazy { createKey() }

    protected abstract fun createKey(): ResourceKey<T>

    abstract fun register()

}