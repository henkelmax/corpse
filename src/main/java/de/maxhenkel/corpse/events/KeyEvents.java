package de.maxhenkel.corpse.events;

import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import de.maxhenkel.corpse.proxy.ClientProxy;
import de.maxhenkel.corpse.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class KeyEvents {

    @SubscribeEvent
    public void onInput(InputEvent.KeyInputEvent event) {
        if (ClientProxy.KEY_DEATH_HISTORY.isPressed()) {
            CommonProxy.simpleNetworkWrapper.sendToServer(new MessageRequestDeathHistory());
        }
    }

}
