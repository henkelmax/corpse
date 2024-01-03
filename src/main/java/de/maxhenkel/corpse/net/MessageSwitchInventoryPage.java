package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageSwitchInventoryPage implements Message {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "switch_inventory_page");

    private int page;

    public MessageSwitchInventoryPage() {

    }

    public MessageSwitchInventoryPage(int page) {
        this.page = page;
    }


    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.containerMenu instanceof CorpseAdditionalContainer containerCorpse) {
            containerCorpse.setSlots(page * 54);
        }
    }

    @Override
    public MessageSwitchInventoryPage fromBytes(FriendlyByteBuf buf) {
        page = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(page);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
