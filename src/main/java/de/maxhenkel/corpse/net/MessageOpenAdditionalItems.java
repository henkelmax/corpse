package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import de.maxhenkel.corpse.gui.Guis;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MessageOpenAdditionalItems implements Message {

    public MessageOpenAdditionalItems() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        if (!(context.getSender().containerMenu instanceof CorpseInventoryContainer)) {
            return;
        }
        Guis.openAdditionalItems(context.getSender(), (CorpseInventoryContainer) context.getSender().containerMenu);
    }

    @Override
    public MessageOpenAdditionalItems fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }
}
