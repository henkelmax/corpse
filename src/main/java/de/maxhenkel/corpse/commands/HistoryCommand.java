package de.maxhenkel.corpse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;

public class HistoryCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalBuilder = Commands.literal("deathhistory").requires((commandSource) -> commandSource.hasPermission(2));

        literalBuilder.then(Commands.argument("player", EntityArgument.player()).executes((commandSource) -> {
            ServerPlayerEntity player = EntityArgument.getPlayer(commandSource, "player");
            boolean success = MessageRequestDeathHistory.sendDeathHistory(commandSource.getSource().getPlayerOrException(), player.getUUID());
            return success ? 1 : 0;
        })).then(Commands.argument("player_uuid", UUIDArgument.uuid()).executes((commandSource) -> {
            UUID player = UUIDArgument.getUuid(commandSource, "player_uuid");
            boolean success = MessageRequestDeathHistory.sendDeathHistory(commandSource.getSource().getPlayerOrException(), player);
            return success ? 1 : 0;
        }));

        dispatcher.register(literalBuilder);
    }

}
