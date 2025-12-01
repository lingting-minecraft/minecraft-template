package live.lingting.minecraft.listener

import live.lingting.minecraft.eunums.AttackResult
import live.lingting.minecraft.eunums.ClickBlockResult
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

/**
 * @author lingting 2025/11/21 14:29
 */
interface ClickListener : BasicListener {

    fun onLeftClickBlock(player: Player, stack: ItemStack, pos: BlockPos): ClickBlockResult {
        return ClickBlockResult.PASS
    }

    fun onLeftClickEntity(player: Player, stack: ItemStack, target: Entity): AttackResult {
        return AttackResult.PASS
    }

}