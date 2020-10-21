package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corelib.net.Message;
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

    public static boolean sendDeathHistory(ServerPlayerEntity player) {
        return sendDeathHistory(player, player.getUniqueID());
    }

    public static boolean sendDeathHistory(ServerPlayerEntity playerToSend, UUID playerUUID) {
        List<Death> deaths = DeathManager.getDeaths(playerToSend.getServerWorld(), playerUUID);
        if (deaths == null) {
            return false;
        }
        Main.SIMPLE_CHANNEL.sendTo(new MessageOpenHistory(deaths), playerToSend.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        return true;
    }
}
