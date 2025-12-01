package live.lingting.minecraft.launch.template

import live.lingting.framework.util.ClassUtils
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.item.IWeaponsItem
import live.lingting.minecraft.item.ItemSource
import live.lingting.minecraft.launch.model.NItemModel
import live.lingting.minecraft.template.weapons.EnergyWeaponsItem
import live.lingting.minecraft.template.weapons.MeleeWeaponsItem
import live.lingting.minecraft.textures.Textures

/**
 * @author lingting 2025/11/29 15:17
 */
class WeaponsItemModel : NItemModel() {

    override val types: Collection<Class<out ItemSource>>
        get() = ClassUtils.scan(MeleeWeaponsItem::class.java.packageName, IWeaponsItem::class.java)

    override fun register() {
        if (source.isSuper(MeleeWeaponsItem::class)) {
            layer0(Textures.ITEM_MELEE_WEAPONS)
        } else if (source.isSuper(EnergyWeaponsItem::class)) {
            layer0(Textures.ITEM_ENERGY_WEAPONS)
        }
    }

}