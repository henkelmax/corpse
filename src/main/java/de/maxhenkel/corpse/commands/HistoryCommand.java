package de.maxhenkel.corpse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class HistoryCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder = Commands.literal("deathhistory").requires((source) -> source.hasPermission(2));

        literalBuilder.then(Commands.argument("player", EntityArgument.player()).executes((commandSource) -> {
            ServerPlayer player = EntityArgument.getPlayer(commandSource, "player");
            boolean success = MessageRequestDeathHistory.sendDeathHistory(commandSource.getSource().getPlayerOrException(), player.getUUID());
            return success ? 1 : 0;
        })).then(Commands.argument("player_uuid", UuidArgument.uuid()).executes((commandSource) -> {
            UUID player = UuidArgument.getUuid(commandSource, "player_uuid");
            boolean success = MessageRequestDeathHistory.sendDeathHistory(commandSource.getSource().getPlayerOrException(), player);
            return success ? 1 : 0;
        }));

        dispatcher.register(literalBuilder);
    }

}
