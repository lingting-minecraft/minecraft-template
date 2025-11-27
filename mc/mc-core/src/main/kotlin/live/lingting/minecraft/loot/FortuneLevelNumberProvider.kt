package live.lingting.minecraft.loot

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import live.lingting.framework.value.WaitValue
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull

/**
 * @author lingting 2025/11/27 23:56
 */
class FortuneLevelNumberProvider(
    val basic: Float,
    val step: Float,
    /**
     * 大于此等级的时运附魔才会递增
     */
    val minLevel: Int = 0
) : BasicNumberProvider() {

    companion object {

        @JvmField
        val CODEC: MapCodec<FortuneLevelNumberProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.FLOAT.fieldOf("base").forGetter { it.basic },
                Codec.FLOAT.optionalFieldOf("step", 1f).forGetter { it.step },
                Codec.INT.optionalFieldOf("min_level", 0).forGetter { it.minLevel }
            ).apply(instance) { base, step, minLevel ->
                FortuneLevelNumberProvider(base, step, minLevel)
            }
        }

        @JvmField
        val TYPE = WaitValue.of<Supplier<LootNumberProviderType>>()

        @JvmStatic
        @JvmOverloads
        fun step(step: Float = 1f, basic: Float = 1f, minLevel: Int = 0): FortuneLevelNumberProvider {
            return FortuneLevelNumberProvider(basic, step, minLevel)
        }

    }

    override fun getType(): LootNumberProviderType {
        return TYPE.notNull().get()
    }

    override fun getFloat(lootContext: LootContext): Float {
        val stack = lootContext.getParamOrNull(LootContextParams.TOOL)
        val enchantments = stack?.components?.get(DataComponents.ENCHANTMENTS)
        if (enchantments == null) {
            return basic
        }
        val lookup = lootContext.resolver.lookup(Registries.ENCHANTMENT).getOrNull()
        val reference = lookup?.get(Enchantments.FORTUNE)?.getOrNull()
        if (reference == null) {
            return basic
        }
        val level = enchantments.getLevel(reference)
        return basic + (level * step)
    }

}