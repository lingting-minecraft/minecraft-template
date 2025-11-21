package live.lingting.minecraft.item

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.world.IWorld
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

/**
 * @author lingting 2025/11/6 19:23
 */
abstract class IItem : Item, IWorld {

    companion object {

        const val PREFIX = "item."

        @JvmStatic
        fun id(v: String) = PREFIX + v

        @JvmStatic
        fun <I> create(cls: Class<I>, properties: Properties): I {
            return ClassUtils.newInstance(cls, false, properties)
        }

    }

    protected val log = logger()

    constructor(p: Properties) : super(p)

    override val id: String = IWorld.id(javaClass)!!

    override fun i18nNameKey(): String? {
        return I18n.ITEM.find(id)?.key
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        return super.useOn(context)
    }

}