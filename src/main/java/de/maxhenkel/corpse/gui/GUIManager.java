package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.EntityCorpse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.UUID;

public class GUIManager {
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        GUIRegistry.register(new ResourceLocation(Main.MODID, "corpse"), openContainer -> {
            EntityPlayerSP player = Minecraft.getInstance().player;
            PacketBuffer buffer = openContainer.getAdditionalData();
            boolean isHistory = buffer.readBoolean();
            if (isHistory) {
                Death death = Death.fromNBT(buffer.readCompoundTag());
                return new GUICorpse(player.inventory, EntityCorpse.createFromDeath(player, death), player.abilities.isCreativeMode);
            } else {
                UUID uuid = new UUID(buffer.readLong(), buffer.readLong());
                EntityCorpse entity = player.world.getEntities(EntityCorpse.class, corpse -> corpse.getUniqueID().equals(uuid)).stream().findFirst().get();
                return new GUICorpse(player.inventory, entity, true);
            }
        });
    }

    public static void openCorpseGUI(EntityPlayerMP player, EntityCorpse corpse) {
        NetworkHooks.openGui(player, new InteractionObjectCorpse(corpse, true), packetBuffer -> {
            packetBuffer.writeBoolean(false);
            packetBuffer.writeLong(corpse.getUniqueID().getMostSignificantBits());
            packetBuffer.writeLong(corpse.getUniqueID().getLeastSignificantBits());
        });
    }

    public static void openCorpseGUI(EntityPlayerMP player, UUID uuid) {
        Death death = DeathManager.getDeath(player, uuid);
        if (death == null) {
            return;
        }
        EntityCorpse corpse = EntityCorpse.createFromDeath(player, death);
        NetworkHooks.openGui(player, new InteractionObjectCorpse(corpse, player.abilities.isCreativeMode), packetBuffer -> {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeCompoundTag(death.toNBT());
        });
    }

}
