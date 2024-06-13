package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import de.maxhenkel.corpse.gui.Guis;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageOpenAdditionalItems implements Message<MessageOpenAdditionalItems> {

    public static final CustomPacketPayload.Type<MessageOpenAdditionalItems> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "open_additional_items"));

    public MessageOpenAdditionalItems() {

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
        if (!(sender.containerMenu instanceof CorpseInventoryContainer)) {
            return;
        }
        Guis.openAdditionalItems(sender, (CorpseInventoryContainer) sender.containerMenu);
    }

    @Override
    public MessageOpenAdditionalItems fromBytes(RegistryFriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public Type<MessageOpenAdditionalItems> type() {
        return TYPE;
    }
}
