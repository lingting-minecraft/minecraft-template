package live.lingting.minecraft.command

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import live.lingting.minecraft.i18n.I18n
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

/**
 * @author lingting 2025/11/24 17:24
 */
class HelloCommand : BasicCommand() {

    override fun generate(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("hello")
            .executes {
                it.source.sendSuccess({ I18n.MOD_TITLE.translatable() }, false)
                SINGLE_SUCCESS
            }
            .then(Commands.literal("-s").executes {
                it.source.sendSuccess({
                    I18n.MOD_TITLE.translatable()
                        .withStyle(ChatFormatting.GOLD)
                }, true)
                SINGLE_SUCCESS
            })
    }

}