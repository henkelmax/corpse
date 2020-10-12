package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import de.maxhenkel.corpse.gui.Guis;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageOpenAdditionalItems implements Message {

    public MessageOpenAdditionalItems() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        if (!(context.getSender().openContainer instanceof CorpseInventoryContainer)) {
            return;
        }
        Guis.openAdditionalItems(context.getSender(), (CorpseInventoryContainer) context.getSender().openContainer);
    }

    @Override
    public MessageOpenAdditionalItems fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}
