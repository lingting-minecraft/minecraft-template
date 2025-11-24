package live.lingting.minecraft.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack

/**
 * @author lingting 2025/11/24 17:23
 */
abstract class BasicCommand {

    abstract fun generate(): LiteralArgumentBuilder<CommandSourceStack>

}