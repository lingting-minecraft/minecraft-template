package live.lingting.minecraft.block

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.i18n.I18nLocale
import live.lingting.minecraft.world.IWorld
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

/**
 * @author lingting 2025/11/4 19:46
 */
abstract class IBlock : Block, EntityBlock, IWorld {

    companion object {

        const val PREFIX = "block."

        @JvmStatic
        fun id(v: String) = PREFIX + v

        @JvmStatic
        fun <I> create(cls: Class<I>, properties: Properties): I {
            return ClassUtils.newInstance(cls, false, properties)
        }

    }

    protected val log = logger()

    constructor(p: Properties) : super(p)

    override val id: String = IWorld.id(javaClass)!!

    override val i18nDesc: I18nLocale?
        get() = I18n.BLOCK.find(id)

    // region mc

    override fun appendHoverText(
        stack: ItemStack,
        tooltip: Item.TooltipContext,
        components: MutableList<Component?>,
        flag: TooltipFlag
    ) {
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return IBlockEntity.findByBlock(javaClass)?.create(pos, state)
    }

    // endregion

}