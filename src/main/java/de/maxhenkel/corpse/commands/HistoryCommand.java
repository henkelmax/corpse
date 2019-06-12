package de.maxhenkel.corpse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

public class HistoryCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalBuilder = Commands.literal("deathhistory").requires((commandSource) -> commandSource.hasPermissionLevel(2));

        literalBuilder.then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            ServerPlayerEntity player = EntityArgument.getPlayer(commandSource, "target");
            MessageRequestDeathHistory.sendDeathHistory(commandSource.getSource().asPlayer(), player.getUniqueID());
            return 1;
        }));

        dispatcher.register(literalBuilder);
    }

}
