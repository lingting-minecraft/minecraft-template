package live.lingting.minecraft.recipes

import live.lingting.minecraft.kt.location
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeOutput

/**
 * @author lingting 2025/11/21 18:12
 */
abstract class ArrayRecipeProvider : BasicRecipeProvider {

    open val prefix = javaClass.name.hashCode().toString()

    constructor() : super()

    override fun buildRecipes(output: RecipeOutput) {
        all().forEachIndexed { i, b ->
            b.save(output, "${prefix}_$i".location())
        }
    }

    abstract fun all(): Collection<RecipeBuilder>

}