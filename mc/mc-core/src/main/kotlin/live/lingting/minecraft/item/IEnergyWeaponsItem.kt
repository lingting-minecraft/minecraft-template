package live.lingting.minecraft.item

import live.lingting.minecraft.data.WeaponsData
import net.minecraft.world.item.Tier
import net.minecraft.world.item.component.ItemAttributeModifiers

/**
 * 能量武器
 * 由于没有 发射出去的实体, 所以伤害全部加在自己身上用于计算
 * @author lingting 2025/11/29 17:15
 */
abstract class IEnergyWeaponsItem : IRangedWeapons {

    constructor(data: WeaponsData, modifiers: ItemAttributeModifiers, p: Properties) : super(data, modifiers, p)

    /**
     * @param durability 基础耐久, 为空或小于1 无线耐久
     * @see live.lingting.minecraft.util.WeaponsUtils.speedFromInterval
     */
    constructor(cls: Class<out IEnergyWeaponsItem>, data: WeaponsData, durability: Int?, p: Properties) : super(
        cls,
        data,
        durability,
        p
    )

    constructor(cls: Class<out IEnergyWeaponsItem>, tier: Tier, p: Properties) : super(cls, tier, p)

}