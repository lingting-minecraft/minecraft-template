package live.lingting.minecraft.recipes

import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.data.BasicDataProvider
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * @author lingting 2025/11/21 17:55
 */
abstract class BasicRecipeProvider : RecipeProvider, BasicDataProvider {

    constructor(o: PackOutput, f: CompletableFuture<HolderLookup.Provider>) : super(o, f)

    private val itemsValue = WaitValue.of<Supplier<List<Item>>>()

    override val items
        get() = itemsValue.notNull().get()

    override fun setItems(value: Supplier<List<Item>>) = itemsValue.update(value)

    private val blocksValue = WaitValue.of<Supplier<List<Block>>>()

    override val blocks
        get() = blocksValue.notNull().get()

    override fun setBlocks(value: Supplier<List<Block>>) = blocksValue.update(value)

    abstract override fun buildRecipes(output: RecipeOutput)

}