package live.lingting.minecraft

import live.lingting.minecraft.block.IBlockEntity
import live.lingting.minecraft.eunums.ActiveEnum
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import java.util.concurrent.atomic.AtomicLong

/**
 * @author lingting 2025/11/21 11:32
 */
class PanelNodeBlockEntity : IBlockEntity {

    companion object {

        @JvmField
        val TYPES = arrayOf(PanelNodeBlock::class)

        const val TAG_COUNTER = "counter"

    }

    constructor(cls: Class<out IBlockEntity>, pos: BlockPos, blockState: BlockState) : super(cls, pos, blockState)

    val counter = AtomicLong(0L)

    override fun saveAdditional(
        tag: CompoundTag,
        provider: HolderLookup.Provider
    ) {
        super.saveAdditional(tag, provider)
        tag.putLong(TAG_COUNTER, counter.get())
    }

    override fun loadAdditional(
        tag: CompoundTag,
        provider: HolderLookup.Provider
    ) {
        super.loadAdditional(tag, provider)
        val l = tag.getLong(TAG_COUNTER)
        counter.set(l)
    }


    fun active(level: Level, pos: BlockPos, state: BlockState = level.getBlockState(pos)) {
        counter.incrementAndGet();
        if (!state.getValue(ActiveEnum.PROPERTY).isActivated) {
            val value = state.setValue(ActiveEnum.PROPERTY, ActiveEnum.ACTIVATED)
            level.setBlock(pos, value, Block.UPDATE_CLIENTS)
        }
    }

    fun inactive(level: Level, pos: BlockPos, state: BlockState = level.getBlockState(pos)) {
        counter.incrementAndGet();
        if (state.getValue(ActiveEnum.PROPERTY).isActivated) {
            val value = state.setValue(ActiveEnum.PROPERTY, ActiveEnum.INACTIVATED)
            level.setBlock(pos, value, Block.UPDATE_CLIENTS)
        }
    }

}