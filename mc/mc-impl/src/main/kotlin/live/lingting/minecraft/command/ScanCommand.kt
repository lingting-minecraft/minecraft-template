package live.lingting.minecraft.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import live.lingting.minecraft.util.Vec3iUtils.yRange
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.blocks.BlockInput
import net.minecraft.commands.arguments.blocks.BlockStateArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

/**
 * @author lingting 2025/11/24 17:43
 */
class ScanCommand : BasicCommand() {

    val argName = "block"

    override fun generate(
        selection: Commands.CommandSelection,
        context: CommandBuildContext
    ): LiteralArgumentBuilder<CommandSourceStack> {
        val argGet = { f: Boolean ->
            Commands.argument(argName, BlockStateArgument.block(context))
                .executes { onScan(it, f) }
        }
        // 异步 从触发人的坐标开始扫描 直到扫描到指定id的方块 或者超出了 范围
        return Commands.literal("scan")
            .then(argGet(false))
            .then(
                Commands.literal("-f")
                    .then(argGet(true))
            )
    }

    fun onScan(context: CommandContext<CommandSourceStack>, force: Boolean): Int {
        val pos = pos(context)
        val level = context.source.level
        val argument = context.getArgument(argName, BlockInput::class.java)
        val target = argument.state
        // 8个区块 在一个平面的所有坐标
        val range = pos.yRange(16 * 8, self = true)
        val list = mutableListOf<Vec3i>()
        for (i in range) {
            val v = scan(level, i, force, target)
            if (v != null) {
                list.add(v)
                break
            }
        }

        if (list.isEmpty()) {
            context.source.sendSystemMessage(Component.literal("未找到方块"))
        } else {
            val v = list.first()
            context.source.sendSystemMessage(
                Component.literal("找到方块, 位置: ${v.x},${v.y},${v.z} ; 点击传送")
                    .withStyle {
                        it.withColor(ChatFormatting.GREEN)
                            .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s ${v.x} ${v.y} ${v.z}"))
                    })
        }
        return SUCCESS
    }

    // 扫描指定坐标所有 y 轴位置方块, 返回匹配到的第一个指定方块
    fun scan(level: ServerLevel, i: Vec3i, force: Boolean, target: BlockState): Vec3i? {
        for (y in level.minBuildHeight..level.maxBuildHeight) {
            val pos = BlockPos(i.x, y, i.z)
            if (!level.isLoaded(pos)) {
                // 不强制加载, 跳过
                if (!force) {
                    continue
                }
            }
            val state = level.getBlockState(pos)
            if (state.`is`(target.block)) {
                return pos
            }
        }
        return null
    }

    fun pos(context: CommandContext<CommandSourceStack>): Vec3i {
        val source = context.source
        val entity = source.entity
        if (entity != null) {
            return Vec3i(entity.blockX, entity.blockY, entity.blockZ)
        }
        return source.level.sharedSpawnPos
    }
}