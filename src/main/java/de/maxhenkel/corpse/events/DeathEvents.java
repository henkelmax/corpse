package de.maxhenkel.corpse.events;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

public class DeathEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerDeath(LivingDropsEvent event) {
        if (event.isCanceled()) {
            return;
        }

        Entity entity = event.getEntity();

        if (entity.world.isRemote) {
            return;
        }

        if (!(entity instanceof ServerPlayerEntity)) {
            return;
        }

        try {
            Collection<ItemEntity> drops = event.getDrops();

            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();

            NonNullList<ItemStack> stacks = NonNullList.create();

            for (ItemEntity item : drops) {
                if (!item.getItem().isEmpty()) {
                    stacks.add(item.getItem());
                }
            }

            drops.clear();

            Death death = Death.fromPlayer(player, stacks);
            DeathManager.addDeath(player, death);

            player.world.addEntity(CorpseEntity.createFromDeath(player, death));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
