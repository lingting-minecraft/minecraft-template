package live.lingting.minecraft.data

import com.mojang.serialization.Codec
import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.DigestUtils
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import java.util.function.Supplier

/**
 * @author lingting 2025/11/30 0:40
 */
@Suppress("UNCHECKED_CAST")
abstract class BasicComponentData {

    companion object {

        @JvmStatic
        fun <T : BasicComponentData> name(cls: Class<T>): String =
            "${cls.simpleName}_${DigestUtils.md5Hex(cls.name)}".lowercase()

        @JvmStatic
        fun <T : BasicComponentData> codec(cls: Class<T>): Codec<T> {
            return ClassUtils.field(cls, "CODEC")!!.get(null) as Codec<T>
        }

        @JvmStatic
        fun <T : BasicComponentData> streamCodec(cls: Class<T>): StreamCodec<in RegistryFriendlyByteBuf, T>? {
            return ClassUtils.field(cls, "STREAM_CODEC")?.get(null) as StreamCodec<in RegistryFriendlyByteBuf, T>?
        }

        @JvmStatic
        fun <T : BasicComponentData> upsert(cls: Class<T>, supplier: Supplier<DataComponentType<T>>) {
            val method = ClassUtils.method(cls, "setType", Supplier::class.java)
            method?.invoke(null, supplier)
        }

    }

}