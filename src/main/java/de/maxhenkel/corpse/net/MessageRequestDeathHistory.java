package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.CorpseMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public class MessageRequestDeathHistory implements Message<MessageRequestDeathHistory> {

    public static final CustomPacketPayload.Type<MessageRequestDeathHistory> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CorpseMod.MODID, "request_death_history"));

    public MessageRequestDeathHistory() {

    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        sendDeathHistory(sender);
    }

    @Override
    public MessageRequestDeathHistory fromBytes(RegistryFriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public Type<MessageRequestDeathHistory> type() {
        return TYPE;
    }

    public static boolean sendDeathHistory(ServerPlayer player) {
        return sendDeathHistory(player, player.getUUID());
    }

    public static boolean sendDeathHistory(ServerPlayer playerToSend, UUID playerUUID) {
        List<Death> deaths = DeathManager.getDeaths(playerToSend.level(), playerUUID);
        if (deaths == null) {
            return false;
        }
        PacketDistributor.sendToPlayer(playerToSend, new MessageOpenHistory(deaths));
        return true;
    }
}
