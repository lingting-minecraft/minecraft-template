package live.lingting.minecraft.listener

import java.util.function.Function

/**
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