package live.lingting.minecraft.launch.model

import jakarta.annotation.Resource
import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.launch.provider.ModelProvider
import live.lingting.minecraft.textures.TexturesItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.WallBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.block.state.properties.StairsShape
import net.minecraft.world.level.block.state.properties.WallSide
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder
import net.neoforged.neoforge.client.model.generators.ConfiguredModel
import net.neoforged.neoforge.client.model.generators.ModelFile
import java.util.function.Function

/**
 * @author lingting 2025/10/18 16:47
 */
abstract class NBlockModel : NModel() {

    private val providerValue = WaitValue.of<ModelProvider.BlockModelProvider>()
    var provider
        get() = providerValue.notNull()
        @Resource
        set(value) = providerValue.update(value)

    val models
        get() = provider.models()

    val block
        get() = source as Block

    abstract override val types: Collection<Class<out BlockSource>>

    fun simpleItem(model: ModelFile) {
        provider.simpleBlockItem(block, model)
    }

    @JvmOverloads
    fun cubeAll(textures: TexturesItem, suffix: String? = null): BlockModelBuilder {
        val name = if (suffix.isNullOrBlank()) id else "${id}_$suffix"
        return models.cubeAll(name, textures.location)
    }

    @JvmOverloads
    fun cubeTop(top: TexturesItem, side: TexturesItem, suffix: String? = null): BlockModelBuilder {
        val name = if (suffix.isNullOrBlank()) id else "${id}_$suffix"
        return models.cubeTop(name, side.location, top.location)
    }

    @JvmOverloads
    fun cubeTopBottom(
        top: TexturesItem,
        bottom: TexturesItem,
        side: TexturesItem,
        suffix: String? = null
    ): BlockModelBuilder {
        val name = if (suffix.isNullOrBlank()) id else "${id}_$suffix"
        return models.cubeBottomTop(name, side.location, bottom.location, top.location)
    }

    fun forAllStates(func: Function<BlockState, Array<ConfiguredModel>>) {
        provider.getVariantBuilder(block).forAllStates(func)
    }

    fun forAllStatesByModel(func: Function<BlockState, ModelFile>) {
        forAllStates {
            val model = func.apply(it)
            ConfiguredModel.builder()
                .modelFile(model)
                .build()
        }
    }

    fun forAllStates(model: ModelFile) {
        forAllStatesByModel { model }
    }

    @JvmOverloads
    fun slab(side: TexturesItem, top: TexturesItem = side, bottom: TexturesItem = side, suffix: String? = null) {
        val name = if (suffix.isNullOrBlank()) id else "${id}_$suffix"
        // 下半砖
        val bottomModel = models.slab("${name}_bottom", side.location, bottom.location, top.location)
        // 上半砖
        val topModel = models.slabTop("${name}_top", side.location, bottom.location, top.location)
        // 完整
        val fullModel = cubeTopBottom(top, bottom, side, suffix)
        forAllStatesByModel {
            val type = it.getValue(SlabBlock.TYPE)
            when (type) {
                SlabType.TOP -> topModel
                SlabType.BOTTOM -> bottomModel
                SlabType.DOUBLE -> fullModel
            }
        }
        simpleItem(bottomModel)
    }

    @JvmOverloads
    fun stair(side: TexturesItem, top: TexturesItem = side, bottom: TexturesItem = side, suffix: String? = null) {
        val name = if (suffix.isNullOrBlank()) id else "${id}_$suffix"
        val straightModel = models.stairs("${name}_straight", side.location, bottom.location, top.location)
        val innerModel = models.stairsInner("${name}_inner", side.location, bottom.location, top.location)
        val outerModel = models.stairsInner("${name}_outer", side.location, bottom.location, top.location)
        forAllStatesByModel {
            val shape = it.getValue(StairBlock.SHAPE)
            when (shape) {
                StairsShape.STRAIGHT -> straightModel
                StairsShape.INNER_LEFT -> innerModel
                StairsShape.INNER_RIGHT -> innerModel
                StairsShape.OUTER_LEFT -> outerModel
                StairsShape.OUTER_RIGHT -> outerModel
            }
        }
        simpleItem(straightModel)
    }

    @JvmOverloads
    fun wall(wall: TexturesItem, particle: TexturesItem = wall, suffix: String? = null) {
        val name = if (suffix.isNullOrBlank()) id else "${id}_$suffix"
        val particleKey = "particle"
        val inventoryModel = models.wallInventory("${name}_inventory", wall.location)
            .texture(particleKey, particle.location)
        val postModel = models.wallPost("${name}_post", wall.location)
            .texture(particleKey, particle.location)
        val sideModel = models.wallSide("${name}_side", wall.location)
            .texture(particleKey, particle.location)
        val tallModel = models.wallSideTall("${name}_tall", wall.location)
            .texture(particleKey, particle.location)
        forAllStatesByModel {
            val east = it.getValue(WallBlock.EAST_WALL)
            val north = it.getValue(WallBlock.NORTH_WALL)
            val south = it.getValue(WallBlock.SOUTH_WALL)
            val west = it.getValue(WallBlock.WEST_WALL)

            val isLow = east == WallSide.LOW || north == WallSide.LOW || south == WallSide.LOW || west == WallSide.LOW
            val isTall =
                east == WallSide.TALL || north == WallSide.TALL || south == WallSide.TALL || west == WallSide.TALL
            if (isTall) {
                tallModel
            } else if (isLow) {
                sideModel
            } else {
                postModel
            }
        }
        simpleItem(inventoryModel)
    }

}