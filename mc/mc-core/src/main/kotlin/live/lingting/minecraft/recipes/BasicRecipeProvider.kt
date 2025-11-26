package live.lingting.minecraft.recipes

import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.data.BasicDataProvider
import live.lingting.minecraft.data.RegisterData
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import java.util.concurrent.CompletableFuture

/**
 * @author lingting 2025/11/21 17:55
 */
abstract class BasicRecipeProvider : RecipeProvider, BasicDataProvider {

    constructor(o: PackOutput, f: CompletableFuture<HolderLookup.Provider>) : super(o, f)

    private val registerDataValue = WaitValue.of<RegisterData>()

    override var registerData: RegisterData
        get() = registerDataValue.notNull()
        set(value) = registerDataValue.update(value)

    abstract override fun buildRecipes(output: RecipeOutput)

}