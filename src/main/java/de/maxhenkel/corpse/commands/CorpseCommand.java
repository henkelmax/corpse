package de.maxhenkel.corpse.commands;

import de.maxhenkel.corpse.CorpseLootService;
import de.maxhenkel.corpse.data.CorpseRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CorpseCommand extends CommandBase {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getName() {
        return "corpse";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/corpse list OR /corpse <id> <player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && "list".equalsIgnoreCase(args[0])) {
            listCorpses(server, sender);
            return;
        }

        if (args.length != 2) {
            throw new WrongUsageException(getUsage(sender));
        }

        UUID id = parseUUID(args[0]);
        EntityPlayerMP recipient = getPlayer(server, sender, args[1]);
        CorpseRegistry.Entry entry = CorpseLootService.claimById(server, id, recipient);
        if (entry == null) {
            throw new CommandException("Corpse not found or already claimed: " + id);
        }

        sender.sendMessage(new TextComponentString("Restored corpse " + id + " owned by " + entry.getPlayerName() + " to " + recipient.getName() + "."));
    }

    private void listCorpses(MinecraftServer server, ICommandSender sender) {
        List<CorpseRegistry.Entry> entries = new ArrayList<>(CorpseRegistry.get(server.getWorld(0)).getEntries());
        entries.sort(Comparator.comparingLong(CorpseRegistry.Entry::getTimestamp).reversed());

        if (entries.isEmpty()) {
            sender.sendMessage(new TextComponentString("No recoverable corpses."));
            return;
        }

        sender.sendMessage(new TextComponentString("Recoverable corpses: " + entries.size()));
        for (CorpseRegistry.Entry entry : entries) {
            sender.sendMessage(new TextComponentString(
                    entry.getId()
                            + " | "
                            + DATE_FORMAT.format(new Date(entry.getTimestamp()))
                            + " | "
                            + entry.getPlayerName()
            ));
        }
    }

    private UUID parseUUID(String value) throws CommandException {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            throw new CommandException("Invalid corpse id: " + value);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("list");
            for (CorpseRegistry.Entry entry : CorpseRegistry.get(server.getWorld(0)).getEntries()) {
                suggestions.add(entry.getId().toString());
            }
            return getListOfStringsMatchingLastWord(args, suggestions);
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return new ArrayList<>();
    }
}
