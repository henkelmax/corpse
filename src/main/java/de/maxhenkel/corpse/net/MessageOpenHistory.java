package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class MessageOpenHistory implements Message<MessageOpenHistory> {

    public static final CustomPacketPayload.Type<MessageOpenHistory> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "open_history"));

    private List<Death> deaths;

    public MessageOpenHistory() {

    }

    public MessageOpenHistory(List<Death> deaths) {
        this.deaths = deaths;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.CLIENTBOUND;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void executeClientSide(IPayloadContext context) {
        if (deaths.size() > 0) {
            Minecraft.getInstance().setScreen(new DeathHistoryScreen(deaths));
        } else {
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("message.corpse.no_death_history"), true);
        }
    }

    @Override
    public MessageOpenHistory fromBytes(RegistryFriendlyByteBuf buf) {
        CompoundTag compound = buf.readNbt();
        ListTag list = compound.getListOrEmpty("Deaths");

        deaths = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            list.getCompound(i).ifPresent(e -> {
                deaths.add(Death.read(buf.registryAccess(), e));
            });
        }

        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        CompoundTag compound = new CompoundTag();

        ListTag list = new ListTag();
        for (Death d : deaths) {
            CompoundTag c = d.write(buf.registryAccess(), false);
            list.add(c);
        }

        compound.put("Deaths", list);
        buf.writeNbt(compound);
    }

    @Override
    public Type<MessageOpenHistory> type() {
        return TYPE;
    }
}
