package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageSwitchInventoryPage implements Message<MessageSwitchInventoryPage> {

    public static final CustomPacketPayload.Type<MessageSwitchInventoryPage> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation(Main.MODID, "switch_inventory_page"));

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
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.containerMenu instanceof CorpseAdditionalContainer containerCorpse) {
            containerCorpse.setSlots(page * 54);
        }
    }

    @Override
    public MessageSwitchInventoryPage fromBytes(RegistryFriendlyByteBuf buf) {
        page = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeInt(page);
    }

    @Override
    public Type<MessageSwitchInventoryPage> type() {
        return TYPE;
    }
}
