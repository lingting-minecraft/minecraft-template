package live.lingting.minecraft.launch.model

import jakarta.annotation.Resource
import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.item.IItem
import live.lingting.minecraft.item.ItemSource
import live.lingting.minecraft.launch.provider.ModelProvider
import live.lingting.minecraft.textures.TexturesItem
import net.minecraft.resources.ResourceLocation

/**
 * @author lingting 2025/10/18 16:33
 */
abstract class NItemModel : NModel() {

    private val providerValue = WaitValue.of<ModelProvider.ItemModelProvider>()

    var provider
        get() = providerValue.notNull()
        @Resource
        set(value) = providerValue.update(value)

    val item
        get() = source as IItem

    abstract override val types: Collection<Class<out ItemSource>>

    fun layer0(textures: TexturesItem) {
        val parent = ResourceLocation.withDefaultNamespace("item/generated")
        provider.singleTexture(
            id,
            parent,
            "layer0",
            textures.location
        )
    }

}