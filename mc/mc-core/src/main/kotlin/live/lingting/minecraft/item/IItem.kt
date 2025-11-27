package live.lingting.minecraft.item

import live.lingting.minecraft.world.IWorld
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext

/**
 * @author lingting 2025/11/6 19:23
 */
abstract class IItem : Item, ItemSource, IWorld {

    constructor(p: Properties) : super(p)

    // region mc

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        return super.useOn(context)
    }

    // endregion

}