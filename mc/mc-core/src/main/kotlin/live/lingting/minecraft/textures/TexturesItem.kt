package live.lingting.minecraft.textures

import live.lingting.minecraft.App
import net.minecraft.resources.ResourceLocation

/**
 * @author lingting 2025/10/14 19:07
 */
class TexturesItem(val key: String, val name: String) {

    val path = "$key/$name"

    val location: ResourceLocation by lazy { ResourceLocation.fromNamespaceAndPath(App.modId, path) }

}