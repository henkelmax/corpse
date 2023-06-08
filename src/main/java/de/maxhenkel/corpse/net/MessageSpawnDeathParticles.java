package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;

public class MessageSpawnDeathParticles implements Message {

    private UUID corpseUUID;

    public MessageSpawnDeathParticles() {

    }

    public MessageSpawnDeathParticles(UUID corpseUUID) {
        this.corpseUUID = corpseUUID;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        spawnParticles();
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticles() {
        LocalPlayer player = Minecraft.getInstance().player;
        Optional<CorpseEntity> c = player.level().getEntitiesOfClass(CorpseEntity.class, player.getBoundingBox().inflate(64D), corpseEntity -> corpseEntity.getUUID().equals(corpseUUID)).stream().findAny();
        c.ifPresent(CorpseEntity::spawnDeathParticles);
    }

    @Override
    public MessageSpawnDeathParticles fromBytes(FriendlyByteBuf buf) {
        corpseUUID = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(corpseUUID);
    }
}
