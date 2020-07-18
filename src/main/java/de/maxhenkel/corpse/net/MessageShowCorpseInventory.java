package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseContainerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.UUID;

public class MessageShowCorpseInventory implements Message {

    private UUID playerUUID;
    private UUID deathID;

    public MessageShowCorpseInventory() {

    }

    public MessageShowCorpseInventory(UUID playerUUID, UUID deathID) {
        this.playerUUID = playerUUID;
        this.deathID = deathID;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender().world.getPlayerByUuid(playerUUID);

        if (player instanceof ServerPlayerEntity) {
            openCorpseGUI(context.getSender(), (ServerPlayerEntity) player, deathID);
        }
    }

    public static void openCorpseGUI(ServerPlayerEntity playerToShow, ServerPlayerEntity player, UUID uuid) {
        Death death = DeathManager.getDeath(player, uuid);
        if (death == null) {
            return;
        }
        CorpseEntity corpse = CorpseEntity.createFromDeath(playerToShow, death);
        NetworkHooks.openGui(playerToShow, new CorpseContainerProvider(corpse, playerToShow.abilities.isCreativeMode, true), packetBuffer -> {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeCompoundTag(death.toNBT());
        });
    }

    @Override
    public MessageShowCorpseInventory fromBytes(PacketBuffer buf) {
        playerUUID = new UUID(buf.readLong(), buf.readLong());
        deathID = new UUID(buf.readLong(), buf.readLong());
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(playerUUID.getMostSignificantBits());
        buf.writeLong(playerUUID.getLeastSignificantBits());
        buf.writeLong(deathID.getMostSignificantBits());
        buf.writeLong(deathID.getLeastSignificantBits());
    }
}
