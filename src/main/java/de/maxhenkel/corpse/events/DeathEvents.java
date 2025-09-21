package de.maxhenkel.corpse.events;

import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corelib.death.PlayerDeathEvent;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DeathEvents {

    public DeathEvents() {
        de.maxhenkel.corelib.death.DeathEvents.register();
    }

    @SubscribeEvent
    public void playerDeath(PlayerDeathEvent event) {
        if (Main.SERVER_CONFIG.maxDeathAge.get() != 0) {
            event.storeDeath();
        }
        event.removeDrops();
        // Get the level from the players dimension instead of using the players level directly
        // as this somehow causes the corpse to not spawn when the player is riding an entity
        ServerLevel level = event.getPlayer().server.getLevel(event.getPlayer().serverLevel().dimension());
        if (level == null) {
            // Fallback to the players level
            level = event.getPlayer().serverLevel();
        }
        level.addFreshEntity(CorpseEntity.createFromDeath(level, event.getPlayer(), event.getDeath()));

        deleteOldDeaths(event.getPlayer().serverLevel());
    }

    public static void deleteOldDeaths(ServerLevel serverWorld) {
        int ageInDays = Main.SERVER_CONFIG.maxDeathAge.get();
        if (ageInDays < 0) {
            return;
        }
        long ageInMillis = ((long) ageInDays) * 24L * 60L * 60L * 1000L;
        //TODO Use an executor
        new Thread(() -> DeathManager.removeDeathsOlderThan(serverWorld, ageInMillis)).start();
    }

}
