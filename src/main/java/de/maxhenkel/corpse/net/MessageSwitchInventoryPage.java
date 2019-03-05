package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.gui.ContainerCorpse;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageSwitchInventoryPage implements Message {

    private int page;

    public MessageSwitchInventoryPage() {

    }

    public MessageSwitchInventoryPage(int page) {
        this.page = page;
    }


    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Container container = context.getSender().openContainer;
        if (container instanceof ContainerCorpse) {
            ContainerCorpse containerCorpse = (ContainerCorpse) container;
            containerCorpse.setSlots(page * 54);
        }
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageSwitchInventoryPage fromBytes(PacketBuffer buf) {
        page = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(page);
    }
}
