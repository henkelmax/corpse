package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

public class MessageSpawnDeathParticles implements Message<MessageSpawnDeathParticles> {

    public static final CustomPacketPayload.Type<MessageSpawnDeathParticles> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation(Main.MODID, "spawn_death_particles"));

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
        spawnParticles();
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles() {
        LocalPlayer player = Minecraft.getInstance().player;
        Optional<CorpseEntity> c = player.level().getEntitiesOfClass(CorpseEntity.class, player.getBoundingBox().inflate(64D), corpseEntity -> corpseEntity.getUUID().equals(corpseUUID)).stream().findAny();
        c.ifPresent(CorpseEntity::spawnDeathParticles);
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
