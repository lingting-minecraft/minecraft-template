package live.lingting.minecraft.i18n

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.MethodUtils.isStatic
import live.lingting.minecraft.App
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/**
 * @author lingting 2025/9/16 16:17
 */
data class I18nLocale(
    val langKey: String,
    val source: Map<String, String>
) {

    val key = "${App.modId}.$langKey"

    fun translatable(): MutableComponent {
        val first = ClassUtils.methods(Component::class.java)
            .first {
                it.name == "translatable" && it.isStatic && it.parameterCount == 1
            }
        return first.invoke(null, key) as MutableComponent
    }

    fun translatable(vararg args: Any?): MutableComponent {
        val first = ClassUtils.methods(Component::class.java)
            .first {
                it.name == "translatable" && it.isStatic && it.parameterCount != 1
            }
        return first.invoke(null, key, args) as MutableComponent
    }

}