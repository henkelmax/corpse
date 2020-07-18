package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.Main;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.UUID;

public class MessageRequestDeathHistory implements Message {

    public MessageRequestDeathHistory() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        sendDeathHistory(context.getSender());
    }

    @Override
    public MessageRequestDeathHistory fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }

    public static void sendDeathHistory(ServerPlayerEntity player) {
        sendDeathHistory(player, player.getUniqueID());
    }

    public static void sendDeathHistory(ServerPlayerEntity playerToSend, UUID playerUUID) {
        List<Death> deaths = DeathManager.getDeaths(playerToSend, playerUUID);
        Main.SIMPLE_CHANNEL.sendTo(new MessageOpenHistory(deaths), playerToSend.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}
