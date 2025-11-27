package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils
import live.lingting.minecraft.data.RegisterData
import live.lingting.minecraft.recipes.BasicRecipeProvider
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import java.util.concurrent.CompletableFuture

/**
 * @author lingting 2025/11/21 18:23
 */
class RecipeProvider(
    val classes: List<Class<out BasicRecipeProvider>>,
    val registerData: RegisterData,
    val o: PackOutput,
    val f: CompletableFuture<HolderLookup.Provider>
) : RecipeProvider(o, f) {

    companion object {

        @JvmStatic
        fun register(
            e: GatherDataEvent,
            recipeClasses: List<Class<out BasicRecipeProvider>>,
            registerData: RegisterData
        ) {
            val g = e.generator
            val provider = RecipeProvider(recipeClasses, registerData, g.packOutput, e.lookupProvider)
            e.addProvider(provider)
        }

    }

    override fun buildRecipes(output: RecipeOutput) {
        val args = listOf(o, f, registerData)
        classes.forEach { recipeCls ->
            if (ClassUtils.isSuper(recipeCls, javaClass)) {
                return@forEach
            }
            val provider = ClassUtils.newInstance(recipeCls, true, args)
            provider.registerData = registerData
            provider.buildRecipes(output)
        }
    }

}