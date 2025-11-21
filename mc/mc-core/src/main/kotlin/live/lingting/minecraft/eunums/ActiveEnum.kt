package live.lingting.minecraft.eunums

import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.block.state.properties.EnumProperty

/**
 * @author lingting 2025/10/16 16:16
 */
enum class ActiveEnum : StringRepresentable {
    /**
     * 已创建
     */
    CREATED,

    /**
     * 已激活
     */
    ACTIVATED,

    /**
     * 未激活
     */
    INACTIVATED,

    ;

    companion object {

        @JvmField
        val PROPERTY: EnumProperty<ActiveEnum> = EnumProperty.create("active", ActiveEnum::class.java)

    }

    /**
     * @return 属性序列化名字, 必须是 小写字母和下划线组成
     */
    override fun getSerializedName(): String {
        return name.lowercase()
    }

    val isActivated
        get() = this == ACTIVATED

}