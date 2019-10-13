package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.proxy.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class MessageDeathInventory implements IMessage, IMessageHandler<MessageDeathInventory, IMessage> {

    private Death death;

    public MessageDeathInventory() {

    }

    public MessageDeathInventory(Death death) {
        this.death = death;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(MessageDeathInventory message, MessageContext ctx) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        CommonProxy.setDeathToShow(player, message.death);
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            death = Death.fromNBT(MessageOpenHistory.readTag(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            MessageOpenHistory.writeTag(buf, death.toNBT());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
