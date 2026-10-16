package de.maxhenkel.corpse.integration.accessories;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class AccessoriesIntegration {
    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded("accessories");
        }
        return loaded;
    }
    
    public static void Transfer(ServerPlayer serverPlayer, NonNullList<ItemStack> additionalInventory) {
        @Nullable AccessoriesCapability accessoriesCapability = AccessoriesCapability.get(serverPlayer);
        NonNullList<ItemStack> oldInventory;
        if(accessoriesCapability != null) {
            boolean changed = true;
            while(changed) {
                oldInventory = additionalInventory;
                for (int i = 0; i < additionalInventory.size(); i++) {
                    ItemStack itemStack = additionalInventory.get(i);
                    SlotReference test = accessoriesCapability.attemptToEquipAccessory(itemStack);
                    if(test != null && !test.toString().equals("minecraft:air")) {
                        additionalInventory.set(i, ItemStack.EMPTY);
                    }
                }
                if(oldInventory.equals(additionalInventory) || additionalInventory.isEmpty()) {
                    changed = false;
                } else {
                    accessoriesCapability.updateContainers();
                    accessoriesCapability.clearCachedSlotModifiers();
                    accessoriesCapability.reset(false);
                }
            }
        }
    }
    
}
