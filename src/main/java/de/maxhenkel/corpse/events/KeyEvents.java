package de.maxhenkel.corpse.events;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
@OnlyIn(Dist.CLIENT)
public class KeyEvents {

    @SubscribeEvent
    public void onInput(TickEvent.ClientTickEvent event) {
        if (Main.KEY_DEATH_HISTORY.isPressed()) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageRequestDeathHistory());
        }
    }

}
