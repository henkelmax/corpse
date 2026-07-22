package de.maxhenkel.corpse;

import de.maxhenkel.corpse.data.CorpseRegistry;
import de.maxhenkel.corpse.entities.EntityCorpse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CorpseLootService {

    public static boolean claimFromCorpse(EntityPlayerMP player, EntityCorpse corpse) {
        if (!corpse.hasCorpseId()) {
            player.sendMessage(new TextComponentString("This corpse has no recovery ID."));
            return false;
        }

        CorpseRegistry registry = CorpseRegistry.get(player.world);
        CorpseRegistry.Entry entry = registry.claim(corpse.getCorpseId());
        if (entry == null) {
            removeCorpse(corpse);
            player.sendMessage(new TextComponentString("Corpse loot was already claimed."));
            return false;
        }

        int transferred = giveItems(player, entry.getItems());
        removeCorpse(corpse);
        player.sendMessage(new TextComponentString("Claimed " + transferred + " item stacks from corpse " + entry.getId() + "."));
        return true;
    }

    @Nullable
    public static CorpseRegistry.Entry claimById(MinecraftServer server, UUID id, EntityPlayerMP recipient) {
        CorpseRegistry registry = CorpseRegistry.get(server.getWorld(0));
        CorpseRegistry.Entry entry = registry.claim(id);
        if (entry == null) {
            return null;
        }

        EntityCorpse corpse = findCorpse(server, id);
        if (corpse != null) {
            removeCorpse(corpse);
        }

        giveItems(recipient, entry.getItems());
        return entry;
    }

    public static int giveItems(EntityPlayerMP recipient, NonNullList<ItemStack> items) {
        int transferred = 0;
        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack copy = stack.copy();
            if (!recipient.inventory.addItemStackToInventory(copy) || !copy.isEmpty()) {
                EntityItem dropped = recipient.dropItem(copy, false);
                if (dropped != null) {
                    dropped.setNoPickupDelay();
                    dropped.setOwner(recipient.getName());
                }
            }
            transferred++;
        }
        recipient.inventoryContainer.detectAndSendChanges();
        return transferred;
    }

    @Nullable
    public static EntityCorpse findCorpse(MinecraftServer server, UUID id) {
        for (WorldServer world : server.worlds) {
            if (world == null) {
                continue;
            }

            for (Entity entity : new ArrayList<>(world.loadedEntityList)) {
                if (entity instanceof EntityCorpse) {
                    EntityCorpse corpse = (EntityCorpse) entity;
                    if (id.equals(corpse.getCorpseId())) {
                        return corpse;
                    }
                }
            }
        }
        return null;
    }

    public static List<EntityCorpse> findCorpses(MinecraftServer server, UUID id) {
        List<EntityCorpse> corpses = new ArrayList<>();
        for (WorldServer world : server.worlds) {
            if (world == null) {
                continue;
            }

            for (Entity entity : new ArrayList<>(world.loadedEntityList)) {
                if (entity instanceof EntityCorpse) {
                    EntityCorpse corpse = (EntityCorpse) entity;
                    if (id.equals(corpse.getCorpseId())) {
                        corpses.add(corpse);
                    }
                }
            }
        }
        return corpses;
    }

    public static void removeCorpse(EntityCorpse corpse) {
        corpse.clear();
        corpse.setDead();
    }
}
