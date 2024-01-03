package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.Guis;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.UUID;

public class MessageShowCorpseInventory implements Message {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "show_corpse_inventory");

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
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        Guis.openCorpseGUI(sender, playerUUID, deathID);
    }

    @Override
    public MessageShowCorpseInventory fromBytes(FriendlyByteBuf buf) {
        playerUUID = new UUID(buf.readLong(), buf.readLong());
        deathID = new UUID(buf.readLong(), buf.readLong());
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(playerUUID.getMostSignificantBits());
        buf.writeLong(playerUUID.getLeastSignificantBits());
        buf.writeLong(deathID.getMostSignificantBits());
        buf.writeLong(deathID.getLeastSignificantBits());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
