package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.ClassUtils.isAbstract
import live.lingting.minecraft.App.modId
import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.item.IItem
import live.lingting.minecraft.launch.NeoForgeLaunch
import live.lingting.minecraft.launch.model.NBlockModel
import live.lingting.minecraft.launch.model.NItemModel
import live.lingting.minecraft.launch.model.NModel
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem

/**
 * @author lingting 2025/10/16 18:56
 */
object ModelProvider {

    fun register(
        e: GatherDataEvent,
        items: List<DeferredItem<Item>>,
        blocks: List<DeferredBlock<Block>>
    ) {
        val scan = ClassUtils.scan(NeoForgeLaunch::class.java.packageName, NModel::class.java)
        val itemModels = mutableListOf<NItemModel>()
        val blockModels = mutableListOf<NBlockModel>()

        scan.forEach {
            if (it.isAbstract || it.isInterface) {
                return@forEach
            }
            val model = ClassUtils.newInstance(it, false)
            if (ClassUtils.isSuper(it, NItemModel::class.java)) {
                itemModels.add(model as NItemModel)
            } else {
                blockModels.add(model as NBlockModel)
            }
        }

        e.generator.addProvider(true, ItemModelProvider(e, items, itemModels))
        e.generator.addProvider(true, BlockModelProvider(e, blocks, blockModels))
    }

    class ItemModelProvider internal constructor(
        e: GatherDataEvent,
        val items: List<DeferredItem<Item>>,
        val models: MutableList<NItemModel>
    ) : net.neoforged.neoforge.client.model.generators.ItemModelProvider(
        e.generator.packOutput,
        modId,
        e.existingFileHelper
    ) {

        override fun registerModels() {
            items.forEach { registerModel(it) }
        }

        fun registerModel(itemDef: DeferredItem<Item>) {
            val item = itemDef.get() as IItem
            models.filter { m ->
                m.types.any { ClassUtils.isSuper(item.javaClass, it) }
            }.forEach {
                it.provider = this
                it.source = item
                it.register()
            }
        }

    }

    class BlockModelProvider internal constructor(
        e: GatherDataEvent,
        val blocks: List<DeferredBlock<Block>>,
        val models: MutableList<NBlockModel>,
    ) : BlockStateProvider(e.generator.packOutput, modId, e.existingFileHelper) {

        override fun registerStatesAndModels() {
            blocks.forEach { register(it) }
        }

        fun register(blockDef: DeferredBlock<Block>) {
            val block = blockDef.get() as IBlock
            models.filter { m ->
                m.types.any { ClassUtils.isSuper(block.javaClass, it) }
            }.forEach {
                it.provider = this
                it.source = block
                it.register()
            }
        }
    }

}