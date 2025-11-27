package live.lingting.minecraft.kt

import live.lingting.minecraft.component.range.FloatRange
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

/**
 * @author lingting 2025/11/27 11:01
 */
inline val FloatRange.number: UniformGenerator get() = UniformGenerator.between(first, last)

inline val Number.number: ConstantValue get() = ConstantValue.exactly(toFloat())