package live.lingting.minecraft.util

/**
 * @author lingting 2025/11/29 1:13
 */
object WeaponsUtils {

    /**
     * 将攻击速度值转换为攻击间隔（单位：秒）
     *
     * 公式：interval = 1.0 / (4.0 + speed)
     * 游戏内部会确保分母 ≥ 0.01，防止除零或负数
     */
    fun speedToInterval(speed: Double): Double {
        val effectiveSpeed = (4.0 + speed).coerceAtLeast(0.01) // 最小有效攻速
        return 1.0 / effectiveSpeed
    }

    /**
     * 将期望的攻击间隔（秒）反推所需的 speed 值
     *
     * 公式：speed = (1.0 / interval) - 4.0
     */
    fun speedFromInterval(seconds: Double): Double {
        return 1.0 / seconds - 4.0
    }

}