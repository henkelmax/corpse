package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Main;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;
import java.util.UUID;

public class MessageRequestDeathHistory implements Message {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "request_death_history");

    public MessageRequestDeathHistory() {

    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        sendDeathHistory(sender);
    }

    @Override
    public MessageRequestDeathHistory fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static boolean sendDeathHistory(ServerPlayer player) {
        return sendDeathHistory(player, player.getUUID());
    }

    public static boolean sendDeathHistory(ServerPlayer playerToSend, UUID playerUUID) {
        List<Death> deaths = DeathManager.getDeaths(playerToSend.serverLevel(), playerUUID);
        if (deaths == null) {
            return false;
        }
        PacketDistributor.PLAYER.with(playerToSend).send(new MessageOpenHistory(deaths));
        return true;
    }
}
