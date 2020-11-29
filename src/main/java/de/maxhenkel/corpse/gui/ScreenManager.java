package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.UUID;

public class ScreenManager {
    @OnlyIn(Dist.CLIENT)
    public static void clientSetup() {
        net.minecraft.client.gui.ScreenManager.IScreenFactory factory = (net.minecraft.client.gui.ScreenManager.IScreenFactory<CorpseContainer, CorpseScreen>) (container, playerInventory, name) -> new CorpseScreen(container.getCorpse(), playerInventory, container, name);
        net.minecraft.client.gui.ScreenManager.registerFactory(Main.CONTAINER_TYPE_CORPSE, factory);
    }

    public static void openCorpseGUI(ServerPlayerEntity player, CorpseEntity corpse) {
        NetworkHooks.openGui(player, new CorpseContainerProvider(corpse, true, false), packetBuffer -> {
            packetBuffer.writeBoolean(false);
            packetBuffer.writeLong(corpse.getUniqueID().getMostSignificantBits());
            packetBuffer.writeLong(corpse.getUniqueID().getLeastSignificantBits());
        });
    }

    public static void openCorpseGUI(ServerPlayerEntity playerToShow, ServerPlayerEntity player, UUID uuid) {
        Death death = DeathManager.getDeath(player, uuid);
        if (death == null) {
            return;
        }
        CorpseEntity corpse = CorpseEntity.createFromDeath(playerToShow, death);
        NetworkHooks.openGui(playerToShow, new CorpseContainerProvider(corpse, playerToShow.abilities.isCreativeMode, true), packetBuffer -> {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeCompoundTag(death.toNBT(false));
        });
    }

}
