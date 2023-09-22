package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.ArrayList;
import java.util.List;

public class MessageOpenHistory implements Message {

    private List<Death> deaths;

    public MessageOpenHistory() {

    }

    public MessageOpenHistory(List<Death> deaths) {
        this.deaths = deaths;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void executeClientSide(CustomPayloadEvent.Context context) {
        if (deaths.size() > 0) {
            Minecraft.getInstance().setScreen(new DeathHistoryScreen(deaths));
        } else {
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("message.corpse.no_death_history"), true);
        }
    }

    @Override
    public MessageOpenHistory fromBytes(FriendlyByteBuf buf) {
        CompoundTag compound = buf.readNbt();
        ListTag list = compound.getList("Deaths", 10);

        deaths = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            deaths.add(Death.fromNBT(list.getCompound(i)));
        }

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        CompoundTag compound = new CompoundTag();

        ListTag list = new ListTag();
        for (Death d : deaths) {
            CompoundTag c = d.toNBT(false);
            list.add(c);
        }

        compound.put("Deaths", list);
        buf.writeNbt(compound);
    }
}
