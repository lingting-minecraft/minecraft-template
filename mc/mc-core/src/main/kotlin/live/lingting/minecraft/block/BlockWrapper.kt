package live.lingting.minecraft.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

/**
 * @author lingting 2025/11/6 19:39
 */
interface BlockWrapper {

    companion object {

        @JvmStatic
        fun from(level: Level, pos: BlockPos): BlockWrapper {
            return PosBlockWrapper(level, pos)
        }

        @JvmStatic
        @JvmOverloads
        fun from(pos: BlockPos, state: BlockState, block: Block = state.block): BlockWrapper {
            return AllBlockWrapper(pos, state, block)
        }

        @JvmStatic
        @JvmOverloads
        fun from(vec: Vec3i, state: BlockState, block: Block = state.block): BlockWrapper {
            return from(BlockPos(vec), state, block)
        }

    }

    val pos: BlockPos

    val state: BlockState

    val block: Block

    private class PosBlockWrapper(level: Level, override val pos: BlockPos) : BlockWrapper {

        private val _s by lazy { level.getBlockState(pos) }

        private val _b by lazy { _s.block }

        override val state: BlockState
            get() = _s

        override val block: Block
            get() = _b

    }

    private class AllBlockWrapper(
        override val pos: BlockPos,
        override val state: BlockState,
        override val block: Block
    ) : BlockWrapper

}