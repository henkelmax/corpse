package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageSwitchInventoryPage implements Message {

    private int page;

    public MessageSwitchInventoryPage() {

    }

    public MessageSwitchInventoryPage(int page) {
        this.page = page;
    }


    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Container container = context.getSender().openContainer;
        if (container instanceof CorpseAdditionalContainer) {
            CorpseAdditionalContainer containerCorpse = (CorpseAdditionalContainer) container;
            containerCorpse.setSlots(page * 54);
        }
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
