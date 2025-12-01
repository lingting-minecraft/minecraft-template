package live.lingting.minecraft.data

import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.component.value.ClassNewValue
import live.lingting.minecraft.listener.BasicListener
import kotlin.reflect.KClass

/**
 * @author lingting 2025/12/1 10:41
 */
@Suppress("UNCHECKED_CAST")
class ListenerData(
    private val classes: List<Class<out BasicListener>>,
    private val registerData: RegisterData,
) {

    private val cache = HashMap<KClass<out BasicListener>, List<BasicListener>>()

    /**
     * 从参数以及注册数据中查找所有指定事件的实例
     */
    @Synchronized
    fun <T : BasicListener> find(klass: KClass<T>, vararg args: Any?): List<T> {
        val r = cache.computeIfAbsent(klass) {
            val r = args.mapNotNull { if (it.isSuper(klass)) it as T else null }.toMutableSet()
            val existsInstances = cache.values.flatten()

            // 已注册数据
            (registerData.blocks + registerData.items).forEach {
                if (it.isSuper(klass)) {
                    r.add(it as T)
                }
            }

            val filter = classes.filter { i ->
                if (!isSuper(i, klass.java)) {
                    false
                } else if (r.any { it.isSuper(i) }) {
                    false
                } else {
                    val e = existsInstances.firstOrNull { it.isSuper(i) }
                    if (e == null) {
                        true
                    } else {
                        r.add(e as T)
                        false
                    }
                }
            }

            val value = ClassNewValue(filter as List<Class<out T>>, (r + existsInstances + args).toList())
            val create = value.create()
            r.addAll(create)
            r.toList()
        }

        return r as List<T>
    }

}