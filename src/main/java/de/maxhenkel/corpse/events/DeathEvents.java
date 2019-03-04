package de.maxhenkel.corpse.events;

import de.maxhenkel.corpse.entities.EntityCorpse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;

@Mod.EventBusSubscriber
public class DeathEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerDeath(LivingDropsEvent event) {
        if (event.isCanceled()) {
            return;
        }

        Entity entity = event.getEntity();

        if (!(entity instanceof EntityPlayer)) {
            return;
        }

        if (entity.world.isRemote) {
            return;
        }

        Collection<EntityItem> drops = event.getDrops();

        if (drops.isEmpty()) {
            return;
        }
        try {
            EntityPlayer player = (EntityPlayer) event.getEntity();

            NonNullList<ItemStack> stacks = NonNullList.create();

            for (EntityItem item : drops) {
                if (!item.getItem().isEmpty()) {
                    stacks.add(item.getItem());
                }
            }

            drops.clear();

            EntityCorpse corpse = new EntityCorpse(player.world);
            corpse.setCorpseUUID(player.getUniqueID());
            corpse.setCorpseName(player.getName().getUnformattedComponentText());
            corpse.setItems(stacks);
            corpse.setPosition(player.posX, player.posY, player.posZ);
            corpse.setCorpseRotation(player.rotationYaw);
            player.world.spawnEntity(corpse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
