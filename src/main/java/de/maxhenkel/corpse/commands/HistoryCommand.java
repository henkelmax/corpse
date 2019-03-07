package de.maxhenkel.corpse.commands;

import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class HistoryCommand extends CommandBase {
    @Override
    public String getName() {
        return "deathhistory";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/deathhistory <player>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new WrongUsageException("message.invalid_arguments");
        }
        EntityPlayerMP player = getPlayer(server, sender, args[0]);
        if (sender.getCommandSenderEntity() instanceof EntityPlayerMP) {
            MessageRequestDeathHistory.sendDeathHistory((EntityPlayerMP) sender.getCommandSenderEntity(), player.getUniqueID());
        }
    }
}
