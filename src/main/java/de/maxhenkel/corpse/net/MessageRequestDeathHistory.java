package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corelib.net.NetUtils;
import de.maxhenkel.corpse.Main;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.NetworkEvent;

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
    public MessageRequestDeathHistory fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    public static boolean sendDeathHistory(ServerPlayer player) {
        return sendDeathHistory(player, player.getUUID());
    }

    public static boolean sendDeathHistory(ServerPlayer playerToSend, UUID playerUUID) {
        List<Death> deaths = DeathManager.getDeaths(playerToSend.serverLevel(), playerUUID);
        if (deaths == null) {
            return false;
        }
        NetUtils.sendTo(Main.SIMPLE_CHANNEL, playerToSend, new MessageOpenHistory(deaths));
        return true;
    }
}
