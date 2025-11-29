package live.lingting.minecraft.util

import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import kotlin.jvm.optionals.getOrNull

/**
 * @author lingting 2025/11/29 16:24
 */
object ItemStackUtils {

    fun ItemStack.getEnchantmentLevel(holder: HolderLookup.Provider?, key: ResourceKey<Enchantment>): Int? {
        val enchantments = components.get(DataComponents.ENCHANTMENTS)
        if (enchantments == null) {
            return null
        }
        val lookup = holder?.lookup(Registries.ENCHANTMENT)?.getOrNull()
        if (lookup == null) {
            return null
        }
        val reference = lookup.get(key).getOrNull()
        if (reference == null) {
            return null
        }
        return enchantments.getLevel(reference)
    }

}