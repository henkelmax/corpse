package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.gui.ITransferrable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageTransferItems implements Message {

    public MessageTransferItems() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        AbstractContainerMenu openContainer = context.getSender().containerMenu;
        if ((openContainer instanceof ITransferrable) && !context.getSender().isDeadOrDying()) {
            ITransferrable transferrable = (ITransferrable) openContainer;
            transferrable.transferItems();
        }
    }

    @Override
    public MessageTransferItems fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

}
