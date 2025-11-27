package live.lingting.minecraft.launch.provider

import live.lingting.minecraft.App.modId
import live.lingting.minecraft.data.RegisterData
import net.minecraft.core.HolderLookup
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.data.event.GatherDataEvent

/**
 * @author lingting 2025/11/23 11:28
 */
class BlockTagsProvider(
    e: GatherDataEvent,
    val registerData: RegisterData
) : BlockTagsProvider(e.generator.packOutput, e.lookupProvider, modId, e.existingFileHelper) {

    companion object {

        fun register(e: GatherDataEvent, registerData: RegisterData) {
            e.addProvider(BlockTagsProvider(e, registerData))
        }

    }

    override fun addTags(provider: HolderLookup.Provider) {
        registerData.blockSource.forEach {
            val tags = it.tags
            if (!tags.isNullOrEmpty()) {
                for (key in tags) {
                    tag(key).add(it as Block)
                }
            }
        }
    }

}