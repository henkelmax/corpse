package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.CorpseMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class MessageSpawnDeathParticles implements Message<MessageSpawnDeathParticles> {

    public static final CustomPacketPayload.Type<MessageSpawnDeathParticles> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CorpseMod.MODID, "spawn_death_particles"));

    private UUID corpseUUID;

    public MessageSpawnDeathParticles() {

    }

    public MessageSpawnDeathParticles(UUID corpseUUID) {
        this.corpseUUID = corpseUUID;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.CLIENTBOUND;
    }

    @Override
    public void executeClientSide(IPayloadContext context) {
        ClientNetworking.spawnParticles(corpseUUID);
    }

    @Override
    public MessageSpawnDeathParticles fromBytes(RegistryFriendlyByteBuf buf) {
        corpseUUID = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(corpseUUID);
    }

    @Override
    public Type<MessageSpawnDeathParticles> type() {
        return TYPE;
    }
}
