package live.lingting.minecraft.item

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.i18n.I18nLocale
import live.lingting.minecraft.world.IWorld
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.level.ItemLike

/**
 * @author lingting 2025/11/27 14:52
 */
interface ItemSource : IWorld, ItemLike {

    companion object {

        const val PREFIX = "item."

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
        get() = I18n.ITEM.find(id)

}