package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.CorpseMod;
import de.maxhenkel.corpse.gui.ITransferrable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageTransferItems implements Message<MessageTransferItems> {

    public static final CustomPacketPayload.Type<MessageTransferItems> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CorpseMod.MODID, "transfer_items"));

    public MessageTransferItems() {

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
        if ((sender.containerMenu instanceof ITransferrable transferrable) && !sender.isDeadOrDying()) {
            transferrable.transferItems();
        }
    }

    @Override
    public MessageTransferItems fromBytes(RegistryFriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {

    }

    @Override
    public Type<MessageTransferItems> type() {
        return TYPE;
    }

}
