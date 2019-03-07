package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.Death;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;

public class MessageTeleport extends MessageToServer<MessageTeleport> {

    private Death death;

    public MessageTeleport() {

    }

    public MessageTeleport(Death death) {
        this.death = death;
    }

    @Override
    public void execute(EntityPlayerMP player, MessageTeleport message) {
        boolean canTeleport = player.canUseCommand(player.mcServer.getOpPermissionLevel(), "tp");
        if (canTeleport) {
            if (player.dimension != message.death.getDimension()) {
                player.sendMessage(new TextComponentTranslation("chat.teleport.wrong_dimension"));
            } else {
                player.setPositionAndUpdate(message.death.getPosX(), message.death.getPosY(), message.death.getPosZ());
            }
        } else {
            player.sendMessage(new TextComponentTranslation("chat.teleport.no_permission"));
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            death = Death.fromNBT(MessageOpenHistory.readTag(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            MessageOpenHistory.writeTag(buf, death.toNBT());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
