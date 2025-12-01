package live.lingting.minecraft.recipes

import live.lingting.minecraft.data.BasicDataProvider
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import java.util.Arrays
import java.util.Optional

/**
 * @author lingting 2025/11/21 17:55
 */
abstract class BasicRecipeProvider : BasicDataProvider {

    abstract fun buildRecipes(output: RecipeOutput)

    private fun inventoryTrigger(vararg builders: ItemPredicate.Builder): Criterion<InventoryChangeTrigger.TriggerInstance> {
        return inventoryTrigger(
            *Arrays.stream(builders).map { it.build() }
                .toArray<ItemPredicate> { i -> arrayOfNulls(i) })
    }

    private fun inventoryTrigger(vararg itemPredicates: ItemPredicate): Criterion<InventoryChangeTrigger.TriggerInstance> {
        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
            InventoryChangeTrigger.TriggerInstance(
                Optional.empty(),
                InventoryChangeTrigger.TriggerInstance.Slots.ANY,
                mutableListOf(*itemPredicates)
            )
        )
    }

    protected fun has(count: MinMaxBounds.Ints, item: ItemLike): Criterion<InventoryChangeTrigger.TriggerInstance> {
        return inventoryTrigger(ItemPredicate.Builder.item().of(item).withCount(count))
    }

    protected fun has(itemLike: ItemLike): Criterion<InventoryChangeTrigger.TriggerInstance> {
        return inventoryTrigger(ItemPredicate.Builder.item().of(itemLike))
    }

    protected fun has(tag: TagKey<Item>): Criterion<InventoryChangeTrigger.TriggerInstance> {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag))
    }

    protected fun slabBuilder(category: RecipeCategory, slab: ItemLike, material: Ingredient): RecipeBuilder {
        return ShapedRecipeBuilder.shaped(category, slab, 6)
            .define('#', material)
            .pattern("###")
    }

    /**
     * 用 source 合成 target. 使用台阶合成方案
     */
    protected fun slabBlock(source: ItemLike, target: ItemLike): RecipeBuilder {
        val ingredient = Ingredient.of(source)
        return slabBuilder(RecipeCategory.BUILDING_BLOCKS, target, ingredient)
            .unlockedBy("has_source", has(source))
    }

    protected fun stairsBuilder(category: RecipeCategory, stairs: ItemLike, material: Ingredient): RecipeBuilder {
        return ShapedRecipeBuilder.shaped(category, stairs, 4)
            .define('#', material)
            .pattern("#  ")
            .pattern("## ")
            .pattern("###")
    }

    /**
     * 用 source 合成 target. 使用楼梯合成方案
     */
    protected fun stairsBlock(source: ItemLike, target: ItemLike): RecipeBuilder {
        val ingredient = Ingredient.of(source)
        return stairsBuilder(RecipeCategory.BUILDING_BLOCKS, target, ingredient)
            .unlockedBy("has_source", has(source))
    }

    protected fun wallBuilder(category: RecipeCategory, wall: ItemLike, material: Ingredient): RecipeBuilder {
        return ShapedRecipeBuilder.shaped(category, wall, 6)
            .define('#', material)
            .pattern("###")
            .pattern("###")
    }

    /**
     * 用 source 合成 target. 使用楼梯合成方案
     */
    protected fun wallBlock(source: ItemLike, target: ItemLike): RecipeBuilder {
        val ingredient = Ingredient.of(source)
        return wallBuilder(RecipeCategory.BUILDING_BLOCKS, target, ingredient)
            .unlockedBy("has_source", has(source))
    }

}