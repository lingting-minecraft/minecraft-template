package live.lingting.minecraft.data

import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.component.value.ClassNewValue
import live.lingting.minecraft.listener.BasicListener
import live.lingting.minecraft.world.IWorld
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
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
    private fun <T : BasicListener> findAll(klass: KClass<T>, vararg args: Any?): List<T> {
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

    /**
     * 如果监听者 是 IWorld 的子类, 那么该监听者仅会在对应 资源 触发的事件中被执行
     */
    private fun <T : BasicListener> find(klass: KClass<T>, source: Any, vararg args: Any?): List<T> {
        val isIWorld = source.isSuper(IWorld::class)
        val find = findAll(klass, source, *args)
        return find.filter {
            // 监听者 是 IWorld 的子类
            if (it.isSuper(IWorld::class)) {
                // 仅会在对应 资源 触发的事件中被执行
                isIWorld && it.isSuper(source.javaClass)
            }
            // 不是则在所有情况下都执行
            else {
                true
            }
        }
    }

    fun <T : BasicListener> findItem(klass: KClass<T>, source: ItemStack, vararg args: Any?): List<T> =
        findItem(klass, source.item, *args)

    fun <T : BasicListener> findItem(klass: KClass<T>, source: Item, vararg args: Any?): List<T> =
        find(klass, source, source, *args)

    fun <T : BasicListener> findPlayer(klass: KClass<T>, source: Player, vararg args: Any?): List<T> =
        find(klass, source, source, *args)


}