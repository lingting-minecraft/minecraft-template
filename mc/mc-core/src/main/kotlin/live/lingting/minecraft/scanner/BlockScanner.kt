package live.lingting.minecraft.scanner

import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.block.BlockWrapper
import live.lingting.minecraft.util.LevelUtils.getBlockState
import live.lingting.minecraft.util.LevelUtils.hasChunk
import live.lingting.minecraft.util.Vec3iUtils.yRange
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Predicate

/**
 * @author lingting 2025/10/15 20:31
 */
class BlockScanner private constructor(val type: Type) {

    companion object {

        @JvmField
        val ALL = BlockScanner(Type.ALL);

        @JvmField
        val ANY = BlockScanner(Type.ANY);

    }

    val log = logger()

    @JvmOverloads
    fun scan(
        level: Level, self: BlockPos, radiusMax: Int,
        predicate: Predicate<BlockState> = Predicate { true }
    ): Map<Int, List<BlockWrapper>> {
        val map = mutableMapOf<Int, List<BlockWrapper>>()

        log.debug("[扫描] 中心坐标[{}], 开始", self)
        for (i in 1..radiusMax) {
            val range = self.yRange(i, true)
            val list = mutableListOf<BlockWrapper>()

            for (vec in range) {
                // 区块未加载, 结束
                if (!level.hasChunk(vec)) {
                    log.debug("[扫描] 坐标[{}]未加载, 结束", vec)
                    list.clear()
                    break
                }
                val state = level.getBlockState(vec)
                val matched = predicate.test(state)

                if (!matched) {
                    // 任意方块未匹配, 结束
                    if (type == Type.ALL) {
                        log.debug("[扫描] 坐标[{}]未匹配, 结束", vec)
                        list.clear()
                        break
                    }
                    log.debug("[扫描] 坐标[{}]未匹配, 跳过", vec)
                    continue
                }

                list.add(BlockWrapper.from(vec, state))
            }
            log.debug("[扫描] 中心点: [{}]; 第{}圈扫描到可用方块{}个", self, i, list.size)
            if (list.isEmpty()) {
                break
            }
            map[i] = list
        }

        log.debug("[扫描] 中心坐标[{}], 完成", self)
        return map
    }

    enum class Type {
        // 所有方块都要匹配
        ALL,

        // 任意方块匹配即可
        ANY,
    }

}