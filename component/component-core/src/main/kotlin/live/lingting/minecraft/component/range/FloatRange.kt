package live.lingting.minecraft.component.range

/**
 * 全闭区间浮点数范围
 * @author lingting 2025/11/23 0:38
 */
class FloatRange(
    override val start: Float,
    override val endInclusive: Float,
) : ClosedRange<Float>, OpenEndRange<Float> {

    override val endExclusive: Float = endInclusive + 1

    val first = start

    val last = endInclusive

    override fun contains(value: Float): Boolean = value >= first && value <= endInclusive

    override fun isEmpty(): Boolean = first > last

}