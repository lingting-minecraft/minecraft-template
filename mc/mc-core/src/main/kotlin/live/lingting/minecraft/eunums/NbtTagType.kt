package live.lingting.minecraft.eunums

/**
 * 表示 Minecraft NBT（Named Binary Tag）数据类型的枚举。
 * 每个类型对应一个唯一的字节 ID 和标准名称。
 * @author lingting 2025/10/16 16:16
 */
enum class NbtTagType(
    /**
     * 该 NBT 类型对应的字节 ID。
     *
     * @return 字节 ID（0 到 12）
     */
    val id: Byte,
    val desc: String
) {
    /**
     * TAG_End (ID: 0) - 表示复合标签（Compound）的结束标记。
     * 不包含名称和有效载荷，仅用于结构终止。
     */
    END(0, "TAG_End"),

    /**
     * TAG_Byte (ID: 1) - 有符号 8 位整数（范围：-128 到 127）。
     */
    BYTE(1, "TAG_Byte"),

    /**
     * TAG_Short (ID: 2) - 有符号 16 位整数（范围：-32768 到 32767）。
     */
    SHORT(2, "TAG_Short"),

    /**
     * TAG_Int (ID: 3) - 有符号 32 位整数。
     */
    INT(3, "TAG_Int"),

    /**
     * TAG_Long (ID: 4) - 有符号 64 位整数。
     */
    LONG(4, "TAG_Long"),

    /**
     * TAG_Float (ID: 5) - 32 位 IEEE 754 单精度浮点数。
     */
    FLOAT(5, "TAG_Float"),

    /**
     * TAG_Double (ID: 6) - 64 位 IEEE 754 双精度浮点数。
     */
    DOUBLE(6, "TAG_Double"),

    /**
     * TAG_Byte_Array (ID: 7) - 字节数组。
     */
    BYTE_ARRAY(7, "TAG_Byte_Array"),

    /**
     * TAG_String (ID: 8) - UTF-8 编码的字符串。
     */
    STRING(8, "TAG_String"),

    /**
     * TAG_List (ID: 9) - 同质 NBT 标签列表（所有元素必须为同一类型）。
     */
    LIST(9, "TAG_List"),

    /**
     * TAG_Compound (ID: 10) - 键值对集合，键为字符串，值为任意 NBT 标签（可嵌套）。
     */
    COMPOUND(10, "TAG_Compound"),

    /**
     * TAG_Int_Array (ID: 11) - 整数数组（int[]）。
     */
    INT_ARRAY(11, "TAG_Int_Array"),

    /**
     * TAG_Long_Array (ID: 12) - 长整型数组（long[]）。
     */
    LONG_ARRAY(12, "TAG_Long_Array"),

    ;

    companion object {

        /**
         * 根据字节 ID 查找对应的 NbtTagType 枚举。
         * 
         * @param id 要查找的 ID（0 到 12）
         * @return 对应的枚举，若不存在则抛出 IllegalArgumentException
         */
        fun from(id: Byte): NbtTagType {
            for (type in entries) {
                if (type.id == id) {
                    return type
                }
            }
            throw IllegalArgumentException("No NBT tag type with ID: $id")
        }

    }
}