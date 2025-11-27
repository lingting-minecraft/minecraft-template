package live.lingting.minecraft.block

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.i18n.I18nLocale
import live.lingting.minecraft.world.IWorld
import net.minecraft.core.BlockPos
import net.minecraft.tags.TagKey
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

/**
 * @author lingting 2025/11/27 14:48
 */
interface BlockSource : EntityBlock, IWorld, ItemLike {

    companion object {

        const val PREFIX = "block."

        @JvmStatic
        fun id(v: String) = PREFIX + v

        @JvmStatic
        fun <I> create(cls: Class<I>, properties: Properties): I {
            return ClassUtils.newInstance(cls, false, properties)
        }

    }

    val log
        get() = logger()

    override val i18nDesc: I18nLocale?
        get() = I18n.BLOCK.find(id)

    /**
     * 声明物品可以用哪些工具. 需要配合 requiresCorrectToolForDrops()
     * @see Properties.requiresCorrectToolForDrops
     */
    open val tags: List<TagKey<Block>>?
        get() = null

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return IBlockEntity.findByBlock(javaClass)?.create(pos, state)
    }

}