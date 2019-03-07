package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.GuiHandler;
import de.maxhenkel.corpse.proxy.CommonProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.UUID;

public class MessageShowCorpseInventory extends MessageToServer<MessageShowCorpseInventory> {

    private UUID playerUUID;
    private UUID deathID;

    public MessageShowCorpseInventory() {

    }

    public MessageShowCorpseInventory(UUID playerUUID, UUID deathID) {
        this.playerUUID = playerUUID;
        this.deathID = deathID;
    }

    @Override
    public void execute(EntityPlayerMP player, MessageShowCorpseInventory message) {
        EntityPlayer p = player.world.getPlayerEntityByUUID(message.playerUUID);

        if (p != null && p instanceof EntityPlayerMP) {
            Death death = DeathManager.getDeath((EntityPlayerMP) p, message.deathID);
            CommonProxy.simpleNetworkWrapper.sendTo(new MessageDeathInventory(death), player);
            CommonProxy.setDeathToShow(player, death);
            p.openGui(Main.MODID, GuiHandler.GUI_DEATH_HISTORY, player.world, 0, 0, 0);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerUUID = new UUID(buf.readLong(), buf.readLong());
        deathID = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(playerUUID.getMostSignificantBits());
        buf.writeLong(playerUUID.getLeastSignificantBits());
        buf.writeLong(deathID.getMostSignificantBits());
        buf.writeLong(deathID.getLeastSignificantBits());
    }
}
