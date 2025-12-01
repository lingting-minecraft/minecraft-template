package live.lingting.minecraft.template.weapons

import live.lingting.minecraft.data.WeaponsData
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.item.IEnergyWeaponsItem
import live.lingting.minecraft.item.ItemSource
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

/**
 * 能量武器
 * @author lingting 2025/11/29 0:27
 */
class EnergyWeaponsItem : IEnergyWeaponsItem {

    companion object {

        @JvmField
        val ID = ItemSource.id("energy.weapons")

    }

    constructor(p: Properties) : super(
        EnergyWeaponsItem::class.java,
        WeaponsData(6.0, 1.0, 7.0, penetrationNumber = 3),
        12,
        p
    )

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
        components.add(I18n.ITEM.MELEE_ENERGY_HOVER.translatable())
        val holder = tooltip.registries()
        val basic = getBasicDamage(stack, holder)
        val additional = getAdditionalDamage(stack, basic, holder)
        components.add(I18n.ITEM.WEAPONS_HOVER_DAMAGE.translatable(basic))
        components.add(I18n.ITEM.WEAPONS_HOVER_DAMAGE_ADDITIONAL.translatable(additional))
    }

}