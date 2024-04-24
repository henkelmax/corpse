package de.maxhenkel.corpse.events;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class KeyEvents {

    @SubscribeEvent
    public void onInput(InputEvent.Key event) {
        if (Main.KEY_DEATH_HISTORY.consumeClick()) {
            PacketDistributor.sendToServer(new MessageRequestDeathHistory());
        }
    }

}
