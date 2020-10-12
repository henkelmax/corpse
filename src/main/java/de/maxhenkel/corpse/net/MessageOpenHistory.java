package de.maxhenkel.corpse.net;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

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
    public void executeClientSide(NetworkEvent.Context context) {
        if (deaths.size() > 0) {
            Minecraft.getInstance().displayGuiScreen(new DeathHistoryScreen(deaths));
        } else {
            Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("message.corpse.no_death_history"), true);
        }
    }

    @Override
    public MessageOpenHistory fromBytes(PacketBuffer buf) {
        CompoundNBT compound = buf.readCompoundTag();
        ListNBT list = compound.getList("Deaths", 10);

        deaths = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            deaths.add(Death.fromNBT(list.getCompound(i)));
        }

        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        CompoundNBT compound = new CompoundNBT();

        ListNBT list = new ListNBT();
        for (Death d : deaths) {
            CompoundNBT c = d.toNBT(false);
            list.add(c);
        }

        compound.put("Deaths", list);
        buf.writeCompoundTag(compound);
    }
}
