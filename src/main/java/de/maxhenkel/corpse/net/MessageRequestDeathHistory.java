package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.proxy.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;
import java.util.UUID;

public class MessageRequestDeathHistory extends MessageToServer<MessageRequestDeathHistory> {

    public MessageRequestDeathHistory() {

    }

    @Override
    public void execute(EntityPlayerMP player, MessageRequestDeathHistory message) {
        sendDeathHistory(player);
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static void sendDeathHistory(EntityPlayerMP player) {
        sendDeathHistory(player, player.getUniqueID());
    }

    public static void sendDeathHistory(EntityPlayerMP playerToSend, UUID playerUUID) {
        List<Death> deaths = DeathManager.getDeaths(playerToSend, playerUUID);
        CommonProxy.simpleNetworkWrapper.sendTo(new MessageOpenHistory(deaths), playerToSend);
    }
}
