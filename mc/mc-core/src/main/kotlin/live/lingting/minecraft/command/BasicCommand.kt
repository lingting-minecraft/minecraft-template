package live.lingting.minecraft.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

/**
 * @author lingting 2025/11/24 17:23
 */
abstract class BasicCommand {

    companion object {

        const val SUCCESS = 1

    }

    abstract fun generate(
        selection: Commands.CommandSelection,
        context: CommandBuildContext
    ): LiteralArgumentBuilder<CommandSourceStack>

}