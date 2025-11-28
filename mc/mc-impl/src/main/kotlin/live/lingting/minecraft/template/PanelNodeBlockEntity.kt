package live.lingting.minecraft.template

import live.lingting.minecraft.block.IBlockEntity
import live.lingting.minecraft.eunums.ActiveEnum
import live.lingting.minecraft.i18n.I18n
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
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

    override fun saveTag(tag: CompoundTag, provider: HolderLookup.Provider?) {
        tag.putLong(TAG_COUNTER, counter.get())
    }

    override fun loadTag(tag: CompoundTag, provider: HolderLookup.Provider?) {
        counter.set(tag.getLong(TAG_COUNTER))
    }

    private fun increment(player: Player?) {
        val l = counter.incrementAndGet()
        player?.sendSystemMessage(I18n.BLOCK.PANEL_NODE_COUNTER_INCREMENT.translatable(l))
    }

    fun active(player: Player?, level: Level, pos: BlockPos, state: BlockState = level.getBlockState(pos)) {
        if (!state.getValue(ActiveEnum.PROPERTY).isActivated) {
            increment(player)
            val value = state.setValue(ActiveEnum.PROPERTY, ActiveEnum.ACTIVATED)
            level.setBlock(pos, value, Block.UPDATE_CLIENTS)
        }
    }

    fun inactive(player: Player?, level: Level, pos: BlockPos, state: BlockState = level.getBlockState(pos)) {
        // 由于左键事件在被拒绝后可能会重试导致多次触发. 所以计数器改为仅在状态变更时递增
        if (state.getValue(ActiveEnum.PROPERTY).isActivated) {
            increment(player)
            val value = state.setValue(ActiveEnum.PROPERTY, ActiveEnum.INACTIVATED)
            level.setBlock(pos, value, Block.UPDATE_CLIENTS)
        }
    }

}