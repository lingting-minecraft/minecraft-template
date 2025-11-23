package live.lingting.minecraft.launch.provider

import live.lingting.minecraft.App.modId
import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.component.kt.isSuper
import net.minecraft.core.HolderLookup
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.registries.DeferredBlock

/**
 * @author lingting 2025/11/23 11:28
 */
class BlockTagsProvider(
    e: GatherDataEvent,
    val registerBlocks: List<DeferredBlock<Block>>
) : BlockTagsProvider(e.generator.packOutput, e.lookupProvider, modId, e.existingFileHelper) {

    companion object {
        fun register(e: GatherDataEvent, registerBlocks: List<DeferredBlock<Block>>) {
            e.addProvider(BlockTagsProvider(e, registerBlocks))
        }

    }

    override fun addTags(provider: HolderLookup.Provider) {
        registerBlocks
            .mapNotNull {
                val block = it.get()
                if (block.isSuper(IBlock::class)) {
                    block as IBlock
                } else {
                    null
                }
            }.forEach {
                val tags = it.tags
                if (!tags.isNullOrEmpty()) {
                    for (key in tags) {
                        tag(key).add(it)
                    }
                }
            }
    }

}