package live.lingting.minecraft.i18n

import live.lingting.minecraft.App

/**
 * @author lingting 2025/9/16 16:17
 */
data class I18nLocale(
    val langKey: String,
    val source: Map<String, String>
) {

    val key = "${App.modId}.$langKey"

}