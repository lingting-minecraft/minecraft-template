package live.lingting.minecraft.template.weapons

import live.lingting.minecraft.data.WeaponsData
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.item.IMeleeWeaponsItem
import live.lingting.minecraft.item.ItemSource
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

/**
 * 近战武器
 * @author lingting 2025/11/29 0:28
 */
class MeleeWeaponsItem : IMeleeWeaponsItem {

    companion object {

        @JvmField
        val ID = ItemSource.id("melee.weapons")

    }

    constructor(p: Properties) : super(MeleeWeaponsItem::class.java, WeaponsData(3.0, 1.5), 12, p)

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
        components.add(I18n.ITEM.MELEE_WEAPONS_HOVER.translatable())
        val holder = tooltip.registries()
        val basic = getBasicDamage(stack, holder)
        val additional = getAdditionalDamage(stack, basic, holder)
        components.add(I18n.ITEM.WEAPONS_HOVER_DAMAGE.translatable(basic))
        components.add(I18n.ITEM.WEAPONS_HOVER_DAMAGE_ADDITIONAL.translatable(additional))
    }

}