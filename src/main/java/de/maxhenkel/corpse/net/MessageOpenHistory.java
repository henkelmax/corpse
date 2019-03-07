package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.gui.GUIDeathHistory;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageOpenHistory implements IMessage, IMessageHandler<MessageOpenHistory, IMessage> {

    private List<Death> deaths;

    public MessageOpenHistory() {

    }

    public MessageOpenHistory(List<Death> deaths) {
        this.deaths = deaths;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageOpenHistory message, MessageContext ctx) {
        if (message.deaths.size() > 0) {
            Minecraft.getMinecraft().displayGuiScreen(new GUIDeathHistory(message.deaths));
        } else {
            Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentTranslation("message.no_death_history"), true);
        }
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            NBTTagCompound compound = readTag(buf);

            NBTTagList list = compound.getTagList("Deaths", 10);

            deaths = new ArrayList<>();
            for (int i = 0; i < list.tagCount(); i++) {
                deaths.add(Death.fromNBT(list.getCompoundTagAt(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound compound = new NBTTagCompound();

        NBTTagList list = new NBTTagList();
        for (Death d : deaths) {
            NBTTagCompound c = d.toNBT(false);
            list.appendTag(c);
        }

        compound.setTag("Deaths", list);
        try {
            writeTag(buf, compound);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTag(ByteBuf buf, NBTTagCompound compound) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(compound, outputStream);
        outputStream.flush();
        byte[] data = outputStream.toByteArray();
        outputStream.close();

        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    public static NBTTagCompound readTag(ByteBuf buf) throws IOException {
        int size = buf.readInt();
        byte[] data = new byte[size];
        buf.readBytes(data);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        NBTTagCompound compound = CompressedStreamTools.readCompressed(inputStream);
        inputStream.close();
        return compound;
    }


}
