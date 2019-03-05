package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.gui.GUIManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessageShowCorpseInventory implements Message {

    private UUID uuid;

    public MessageShowCorpseInventory() {

    }

    public MessageShowCorpseInventory(UUID uuid) {
        this.uuid = uuid;
    }


    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        GUIManager.openCorpseGUI(context.getSender(), uuid);
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageShowCorpseInventory fromBytes(PacketBuffer buf) {
        uuid = new UUID(buf.readLong(), buf.readLong());
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }
}
