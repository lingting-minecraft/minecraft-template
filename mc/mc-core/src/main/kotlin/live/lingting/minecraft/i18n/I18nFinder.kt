package live.lingting.minecraft.i18n

import live.lingting.framework.util.ClassUtils

/**
 * @author lingting 2025/9/16 18:49
 */
interface I18nFinder {

    fun find(key: String): I18nLocale? {
        return ClassUtils.fields(javaClass)
            .filter { I18nLocale::class.java == it.type }
            .mapNotNull {
                it.get(this) as I18nLocale?
            }
            .firstOrNull {
                it.langKey == key
            }
    }

}