package de.maxhenkel.corpse.events;

import de.maxhenkel.corelib.death.PlayerDeathEvent;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DeathEvents {

    public DeathEvents() {
        de.maxhenkel.corelib.death.DeathEvents.register();
    }

    @SubscribeEvent()
    public void playerDeath(PlayerDeathEvent event) {
        event.storeDeath();
        event.removeDrops();
        event.getPlayer().world.addEntity(CorpseEntity.createFromDeath(event.getPlayer(), event.getDeath()));
    }

}
