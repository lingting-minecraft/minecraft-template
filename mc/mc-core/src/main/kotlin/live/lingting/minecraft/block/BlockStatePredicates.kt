package live.lingting.minecraft.block

import net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate

/**
 * @author lingting 2025/11/27 14:55
 */
object BlockStatePredicates {

    @JvmField
    val ALWAY_TRUE: StatePredicate = StatePredicate { _, _, _ -> true }

    @JvmField
    val ALWAY_FALSE: StatePredicate = StatePredicate { _, _, _ -> false }

}