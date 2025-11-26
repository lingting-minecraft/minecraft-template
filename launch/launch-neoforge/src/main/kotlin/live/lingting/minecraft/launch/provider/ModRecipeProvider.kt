package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils
import live.lingting.minecraft.component.kt.isSuper
import live.lingting.minecraft.data.BasicDataProvider
import live.lingting.minecraft.data.RegisterData
import net.minecraft.data.recipes.RecipeProvider
import net.neoforged.neoforge.data.event.GatherDataEvent

/**
 * @author lingting 2025/11/21 18:23
 */
object ModRecipeProvider {

    fun register(
        e: GatherDataEvent,
        recipeClasses: List<Class<out RecipeProvider>>,
        registerData: RegisterData
    ) {
        val g = e.generator
        val args = listOf(e, g, g.packOutput, e.lookupProvider)
        recipeClasses.forEach { recipeCls ->
            val provider = ClassUtils.newInstance(recipeCls, true, args)
            if (provider.isSuper(BasicDataProvider::class)) {
                val p = provider as BasicDataProvider
                p.registerData = registerData
            }

            e.addProvider(provider)
        }
    }

}