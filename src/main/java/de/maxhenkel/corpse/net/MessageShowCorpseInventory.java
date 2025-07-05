package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.CorpseMod;
import de.maxhenkel.corpse.gui.Guis;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class MessageShowCorpseInventory implements Message<MessageShowCorpseInventory> {

    public static final CustomPacketPayload.Type<MessageShowCorpseInventory> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CorpseMod.MODID, "show_corpse_inventory"));

    private UUID playerUUID;
    private UUID deathID;

    public MessageShowCorpseInventory() {

    }

    public MessageShowCorpseInventory(UUID playerUUID, UUID deathID) {
        this.playerUUID = playerUUID;
        this.deathID = deathID;
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
        Guis.openCorpseGUI(sender, playerUUID, deathID);
    }

    @Override
    public MessageShowCorpseInventory fromBytes(RegistryFriendlyByteBuf buf) {
        playerUUID = new UUID(buf.readLong(), buf.readLong());
        deathID = new UUID(buf.readLong(), buf.readLong());
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeLong(playerUUID.getMostSignificantBits());
        buf.writeLong(playerUUID.getLeastSignificantBits());
        buf.writeLong(deathID.getMostSignificantBits());
        buf.writeLong(deathID.getLeastSignificantBits());
    }

    @Override
    public Type<MessageShowCorpseInventory> type() {
        return TYPE;
    }
}
