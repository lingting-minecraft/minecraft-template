package live.lingting.minecraft.kt

import live.lingting.minecraft.App.modId
import net.minecraft.resources.ResourceLocation

/**
 * @author lingting 2025/11/21 18:10
 */
fun String.location(): ResourceLocation = ResourceLocation.fromNamespaceAndPath(modId, this)