package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.gui.GUIDeathHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
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
    public void executeServerSide(NetworkEvent.Context context) {

    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        if (deaths.size() > 0) {
            Minecraft.getInstance().displayGuiScreen(new GUIDeathHistory(deaths));
        }else{
            Minecraft.getInstance().player.sendStatusMessage(new TextComponentTranslation("message.no_death_history"), true);
        }
    }

    @Override
    public MessageOpenHistory fromBytes(PacketBuffer buf) {
        NBTTagCompound compound = buf.readCompoundTag();
        NBTTagList list = compound.getList("Deaths", 10);

        deaths = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            deaths.add(Death.fromNBT(list.getCompound(i)));
        }

        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        NBTTagCompound compound = new NBTTagCompound();

        NBTTagList list = new NBTTagList();
        for (Death d : deaths) {
            NBTTagCompound c = d.toNBT(false);
            list.add(c);
        }

        compound.setTag("Deaths", list);
        buf.writeCompoundTag(compound);
    }
}
