package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Main;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

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
    public void executeServerSide(CustomPayloadEvent.Context context) {
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
        Main.SIMPLE_CHANNEL.send(new MessageOpenHistory(deaths), playerToSend.connection.getConnection());
        return true;
    }
}
