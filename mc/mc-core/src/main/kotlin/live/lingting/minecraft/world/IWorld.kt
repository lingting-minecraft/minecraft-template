package live.lingting.minecraft.world

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.FieldUtils.isStatic
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.App
import live.lingting.minecraft.data.CreativeTabs
import org.slf4j.Logger
import kotlin.reflect.KClass

/**
 * @author lingting 2025/11/4 19:47
 */
interface IWorld {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun id(cls: KClass<IWorld>, log: Logger = logger(cls.java)) = id(cls.java, log)

        @JvmStatic
        @JvmOverloads
        fun id(cls: Class<IWorld>, log: Logger = logger(cls)): String? {
            val field = ClassUtils.classField(cls, "ID")
            if (field == null || field.field == null || field.field?.isStatic != true) {
                log.debug("[{}] 类[{}]无法加载, 未获取到ID字段!", App.modId, cls.name)
                return null
            }
            val id = field.field?.get(null)?.toString()
            if (id.isNullOrBlank()) {
                log.debug("[{}] 类[{}]无法加载, ID值异常!", App.modId, cls.name)
                return null
            }
            return id
        }

    }

    val id: String

    val creativeTab: CreativeTabs
        get() = CreativeTabs.MAIN

    fun i18nNameKey(): String?

    fun i18nDescKey(): String? = i18nNameKey()

}