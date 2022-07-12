package de.maxhenkel.corpse.gui;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class Guis {

    public static void openAdditionalItems(ServerPlayer player, CorpseInventoryContainer container) {
        openAdditionalItems(player, container.getCorpse(), container.isEditable(), container.isHistory());
    }

    public static void openAdditionalItems(ServerPlayer player, CorpseEntity corpse, boolean editable, boolean history) {
        NetworkHooks.openScreen(player, new CorpseAdditionalItemsContainerProvider(corpse, editable, history), packetBuffer -> {
            packetBuffer.writeBoolean(history);
            packetBuffer.writeBoolean(corpse.isAdditionalInventoryEmpty());
            if (history) {
                packetBuffer.writeNbt(corpse.getDeath().toNBT(false));
            } else {
                packetBuffer.writeUUID(corpse.getUUID());
            }
        });
    }

    public static void openCorpseGUI(ServerPlayer player, CorpseEntity corpse, boolean editable, boolean history) {
        if (corpse.isMainInventoryEmpty() && !corpse.isEmpty()) {
            openAdditionalItems(player, corpse, editable, history);
        } else {
            NetworkHooks.openScreen(player, new CorpseContainerProvider(corpse, editable, history), packetBuffer -> {
                packetBuffer.writeBoolean(history);
                packetBuffer.writeBoolean(corpse.isAdditionalInventoryEmpty());
                if (history) {
                    packetBuffer.writeNbt(corpse.getDeath().toNBT(false));
                } else {
                    packetBuffer.writeUUID(corpse.getUUID());
                }
            });
        }
    }

    /**
     * Called when actually interacting with the corpse
     *
     * @param player the player
     * @param corpse the corpse
     */
    public static void openCorpseGUI(ServerPlayer player, CorpseEntity corpse) {
        openCorpseGUI(player, corpse, true, false);
    }

    /**
     * Called when opening the corpse inventory from commands or the history
     *
     * @param playerToShow the player that gets the screen
     * @param player       the player the death is from
     * @param uuid         the death id
     */
    public static void openCorpseGUI(ServerPlayer playerToShow, UUID player, UUID uuid) {
        Death death = DeathManager.getDeath(playerToShow.getLevel(), player, uuid);
        if (death == null) {
            return;
        }
        CorpseEntity corpse = CorpseEntity.createFromDeath(playerToShow, death);
        openCorpseGUI(playerToShow, corpse, playerToShow.getAbilities().instabuild, true);
    }

}
