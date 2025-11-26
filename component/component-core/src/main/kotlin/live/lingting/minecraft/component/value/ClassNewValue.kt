package live.lingting.minecraft.component.value

import live.lingting.framework.util.ClassUtils
import live.lingting.minecraft.component.kt.isSuper

/**
 * @author lingting 2025/11/24 14:35
 */
class ClassNewValue<T>(
    val classes: List<Class<out T>>,
    args: List<Any?>
) {

    private val args = mutableListOf<Any?>().apply { addAll(args) }

    private val needCreateClasses = mutableListOf<Class<out T>>().apply { addAll(classes.distinct()) }

    private val instances = mutableListOf<T>()

    fun create(): List<T> {
        if (needCreateClasses.isEmpty()) {
            return instances
        }

        needCreateClasses.removeIf { cls ->
            val constructor = cls.constructors[0]
            val types = mutableListOf<Class<*>>()
            types.addAll(constructor.parameterTypes)
            val cfs = ClassUtils.autowiredClassField(cls)
            val cfsTypes = cfs.map { it.getSetArgTypes().toList() }.flatten()
            types.addAll(cfsTypes)
            val allowCreate = types.distinct().all { ac ->
                // 参数里面有这个类型, 允许创建
                if (args.any { it.isSuper(ac) }) {
                    true
                }
                // 需要创建的类里面有, 不允许创建
                else if (needCreateClasses.contains(ac)) {
                    false
                } else {
                    true
                }
            }
            if (allowCreate) {
                newInstance(cls)
            }
            allowCreate
        }

        return create()
    }


    fun newInstance(cls: Class<out T>): T {
        val args = mutableListOf<Any?>()
        args.addAll(this.args)
        val t = ClassUtils.newInstance(cls, true, args)
        this.args.add(t)
        instances.add(t)
        return t
    }

}