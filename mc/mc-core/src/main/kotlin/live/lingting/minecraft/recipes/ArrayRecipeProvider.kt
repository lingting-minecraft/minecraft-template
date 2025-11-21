package live.lingting.minecraft.recipes

import live.lingting.minecraft.kt.location
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeOutput
import java.util.concurrent.CompletableFuture

/**
 * @author lingting 2025/11/21 18:12
 */
abstract class ArrayRecipeProvider : BasicRecipeProvider {

    open val prefix = javaClass.name.hashCode().toString()

    constructor(o: PackOutput, f: CompletableFuture<HolderLookup.Provider>) : super(o, f)

    override fun buildRecipes(output: RecipeOutput) {
        builderes().forEachIndexed { i, b ->
            b.save(output, "${prefix}_$i".location())
        }
    }

    abstract fun builderes(): Collection<RecipeBuilder>

}