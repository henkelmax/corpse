package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.Main;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.UUID;

public class MessageRequestDeathHistory implements Message {

    public MessageRequestDeathHistory() {

    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        sendDeathHistory(context.getSender());
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageRequestDeathHistory fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }

    public static void sendDeathHistory(EntityPlayerMP player) {
        sendDeathHistory(player, player.getUniqueID());
    }

    public static void sendDeathHistory(EntityPlayerMP playerToSend, UUID playerUUID) {
        List<Death> deaths = DeathManager.getDeaths(playerToSend, playerUUID);
        Main.SIMPLE_CHANNEL.sendTo(new MessageOpenHistory(deaths), playerToSend.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}
