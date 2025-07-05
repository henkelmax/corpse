package de.maxhenkel.corpse.events;

import de.maxhenkel.corpse.CorpseClientMod;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class KeyEvents {

    @SubscribeEvent
    public void onInput(InputEvent.Key event) {
        if (CorpseClientMod.KEY_DEATH_HISTORY.consumeClick()) {
            ClientPacketDistributor.sendToServer(new MessageRequestDeathHistory());
        }
    }

}
