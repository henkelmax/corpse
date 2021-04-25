package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.gui.ITransferrable;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageTransferItems implements Message {

    public MessageTransferItems() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Container openContainer = context.getSender().containerMenu;
        if ((openContainer instanceof ITransferrable) && !context.getSender().isDeadOrDying()) {
            ITransferrable transferrable = (ITransferrable) openContainer;
            transferrable.transferItems();
        }
    }

    @Override
    public MessageTransferItems fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }

}
