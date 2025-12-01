package live.lingting.minecraft.listener

import live.lingting.minecraft.eunums.AttackResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

/**
 * @author lingting 2025/12/1 10:30
 */
interface PlayerListener : BasicListener {

    fun onAttackEntity(player: Player, target: Entity): AttackResult {
        return AttackResult.PASS
    }

}