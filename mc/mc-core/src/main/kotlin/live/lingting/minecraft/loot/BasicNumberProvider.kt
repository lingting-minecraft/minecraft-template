package live.lingting.minecraft.loot

import com.mojang.serialization.MapCodec
import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.DigestUtils
import live.lingting.framework.value.WaitValue
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import java.util.function.Supplier

/**
 * @author lingting 2025/11/28 0:06
 */
@Suppress("UNCHECKED_CAST")
abstract class BasicNumberProvider : NumberProvider {

    companion object {

        @JvmStatic
        fun <T : NumberProvider> name(cls: Class<T>): String =
            "${cls.simpleName}_${DigestUtils.md5Hex(cls.name)}".lowercase()

        @JvmStatic
        fun <T : NumberProvider> codec(cls: Class<T>): MapCodec<T> {
            return ClassUtils.field(cls, "CODEC")!!.get(null) as MapCodec<T>
        }

        @JvmStatic
        fun <T : NumberProvider> upsert(cls: Class<T>, supplier: Supplier<LootNumberProviderType>) {
            val value = ClassUtils.field(cls, "TYPE")!!.get(null) as WaitValue<Supplier<LootNumberProviderType>>
            value.update(supplier)
        }

    }


}