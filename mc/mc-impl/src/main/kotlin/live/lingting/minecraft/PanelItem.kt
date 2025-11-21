package live.lingting.minecraft

import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.minecraft.eunums.ClickBlockResult
import live.lingting.minecraft.item.IItem
import live.lingting.minecraft.listener.LeftClickListener
import live.lingting.minecraft.util.PlayerUtils.isClientSide
import live.lingting.minecraft.util.UseOnContextUtils.isClientSide
import live.lingting.minecraft.util.UseOnContextUtils.targetEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

/**
 * @author lingting 2025/11/6 19:34
 */
class PanelItem : IItem, LeftClickListener {

    companion object {

        @JvmField
        val ID = id("panel")

    }

    constructor(p: Properties) : super(p.also {
        p.stacksTo(128)
    })

    override fun useOn(context: UseOnContext): InteractionResult {
        val entity = context.targetEntity()
        if (!isSuper(entity, PanelNodeBlockEntity::class.java)) {
            return InteractionResult.PASS
        }
        // 数据变化仅在服务端进行
        if (!context.isClientSide) {
            (entity as PanelNodeBlockEntity).active(context.level, context.clickedPos)
        }
        // 确保客户端和服务端同步结果同步
        return InteractionResult.SUCCESS
    }

    override fun onLeftClickBlock(
        player: Player,
        stack: ItemStack,
        pos: BlockPos
    ): ClickBlockResult {
        val level = player.level()
        val entity = level.getBlockEntity(pos)
        if (!isSuper(entity, PanelNodeBlockEntity::class.java)) {
            return ClickBlockResult.PASS
        }
        if (!player.isClientSide) {
            (entity as PanelNodeBlockEntity).inactive(level, pos)
        }
        return ClickBlockResult.REJECT
    }

}