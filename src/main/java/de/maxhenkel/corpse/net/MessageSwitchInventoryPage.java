package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.gui.ContainerCorpse;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

public class MessageSwitchInventoryPage extends MessageToServer<MessageSwitchInventoryPage> {

    private int page;

    public MessageSwitchInventoryPage() {

    }

    public MessageSwitchInventoryPage(int page) {
        this.page = page;
    }

    @Override
    public void execute(EntityPlayerMP player, MessageSwitchInventoryPage message) {
        Container container = player.openContainer;
        if (container instanceof ContainerCorpse) {
            ContainerCorpse containerCorpse = (ContainerCorpse) container;
            containerCorpse.setSlots(message.page * 54);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        page = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(page);
    }
}
