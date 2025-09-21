package de.maxhenkel.corpse.events;

import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corelib.death.PlayerDeathEvent;
import de.maxhenkel.corpse.CorpseMod;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

public class DeathEvents {

    public DeathEvents() {
        de.maxhenkel.corelib.death.DeathEvents.register();
    }

    @SubscribeEvent
    public void playerDeath(PlayerDeathEvent event) {
        if (CorpseMod.SERVER_CONFIG.maxDeathAge.get() != 0) {
            event.storeDeath();
        }
        event.removeDrops();

        ServerPlayer player = event.getPlayer();

        player.level().addFreshEntity(CorpseEntity.createFromDeath(player, event.getDeath()));

        deleteOldDeaths(player.level());
    }

    public static void deleteOldDeaths(ServerLevel serverWorld) {
        int ageInDays = CorpseMod.SERVER_CONFIG.maxDeathAge.get();
        if (ageInDays < 0) {
            return;
        }
        long ageInMillis = ((long) ageInDays) * 24L * 60L * 60L * 1000L;
        //TODO Use an executor
        new Thread(() -> DeathManager.removeDeathsOlderThan(serverWorld, ageInMillis)).start();
    }

}
