package live.lingting.minecraft

import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.i18n.I18nLocale
import live.lingting.minecraft.template.PanelItem
import net.minecraft.world.item.ItemStack
import java.util.function.Supplier

/**
 * @author lingting 2025/11/21 18:49
 */
enum class CreativeTabs(
    val id: String,
    val i18n: I18nLocale,
    val icon: Supplier<ItemStack>
) {

    MAIN("", I18n.MOD_TITLE, { App.getItem(PanelItem.ID).defaultInstance }),

    ;

}