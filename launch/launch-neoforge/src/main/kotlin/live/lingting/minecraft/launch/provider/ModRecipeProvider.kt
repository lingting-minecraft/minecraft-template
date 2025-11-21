package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.data.BasicDataProvider
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem

/**
 * @author lingting 2025/11/21 18:23
 */
object ModRecipeProvider {

    fun register(
        e: GatherDataEvent,
        recipeClasses: MutableList<Class<RecipeProvider>>,
        registerItems: List<DeferredItem<Item>>,
        registerBlocks: List<DeferredBlock<Block>>
    ) {
        val g = e.generator
        val args = listOf(e, g, g.packOutput, e.lookupProvider)
        recipeClasses.forEach { recipeCls ->
            val provider = ClassUtils.newInstance(recipeCls, true, args)
            if (provider.isSuper(BasicDataProvider::class)) {
                val p = provider as BasicDataProvider
                p.setItems { registerItems.map { it.get() } }
                p.setBlocks { registerBlocks.map { it.get() } }
            }

            e.addProvider(provider)
        }
    }

}