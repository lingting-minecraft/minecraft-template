package live.lingting.minecraft.block

import live.lingting.minecraft.world.IWorld
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.BlockState

/**
 * @author lingting 2025/11/27 15:04
 */
abstract class ISlabBlock : SlabBlock, BlockSource, IWorld {

    constructor(p: Properties) : super(p)

    // region mc

    /**
     * 是否跳过相邻面的渲染（实现无缝连接）
     */
    override fun skipRendering(self: BlockState, other: BlockState, direction: Direction): Boolean {
        return super.skipRendering(self, other, direction)
    }

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: Item.TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
    }

    // endregion

}