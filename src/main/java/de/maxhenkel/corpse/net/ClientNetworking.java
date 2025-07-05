package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClientNetworking {

    public static void spawnParticles(UUID corpseUUID) {
        LocalPlayer player = Minecraft.getInstance().player;
        Optional<CorpseEntity> c = player.level().getEntitiesOfClass(CorpseEntity.class, player.getBoundingBox().inflate(64D), corpseEntity -> corpseEntity.getUUID().equals(corpseUUID)).stream().findAny();
        c.ifPresent(CorpseEntity::spawnDeathParticles);
    }

    public static void openCorpseHistory(List<Death> deaths) {
        if (deaths.size() > 0) {
            Minecraft.getInstance().setScreen(new DeathHistoryScreen(deaths));
        } else {
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("message.corpse.no_death_history"), true);
        }
    }

}
