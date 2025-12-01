package live.lingting.minecraft.listener

import java.util.function.Function

/**
 * 如果监听者 是 IWorld 的子类, 那么该监听者仅会在对应 资源 触发的事件中被执行
 * @author lingting 2025/12/1 10:41
 */
interface BasicListener {

    companion object {

        /**
         * 链式调用, 仅pass时继续调用下一个事件
         */
        @JvmStatic
        fun <T, R> onTrigger(listeners: List<T>, func: Function<T, R>, default: R): R {
            for (listener in listeners) {
                val r = func.apply(listener)
                if (r != default) {
                    return r
                }
            }
            return default
        }

    }

}