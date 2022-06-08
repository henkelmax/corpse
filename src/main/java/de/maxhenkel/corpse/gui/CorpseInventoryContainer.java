package de.maxhenkel.corpse.gui;

import com.mojang.datafixers.util.Pair;
import de.maxhenkel.corelib.inventory.ItemListInventory;
import de.maxhenkel.corelib.inventory.LockedSlot;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class CorpseInventoryContainer extends CorpseContainerBase implements ITransferrable {

    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};

    private ItemListInventory mainInventory;
    private ItemListInventory armorInventory;
    private ItemListInventory offHandInventory;

    public CorpseInventoryContainer(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
        super(Main.CONTAINER_TYPE_CORPSE_INVENTORY.get(), id, playerInventory, corpse, editable, history);

        mainInventory = new ItemListInventory(corpse.getDeath().getMainInventory());
        armorInventory = new ItemListInventory(corpse.getDeath().getArmorInventory());
        offHandInventory = new ItemListInventory(corpse.getDeath().getOffHandInventory());

        for (int i = 0; i < 4; i++) {
            int slotIndex = 3 - i;
            addSlot(new LockedSlot(armorInventory, slotIndex, 8 + i * 18, 18, true, !editable) {
                @OnlyIn(Dist.CLIENT)
                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, ARMOR_SLOT_TEXTURES[slotIndex]);
                }
            });
        }

        addSlot(new LockedSlot(offHandInventory, 0, 98, 18, true, !editable) {
            @OnlyIn(Dist.CLIENT)
            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        int k;
        for (k = 0; k < 3; ++k) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new LockedSlot(mainInventory, j + k * 9 + 9, 8 + j * 18, 40 + k * 18, true, !editable));
            }
        }

        for (k = 0; k < 9; ++k) {
            addSlot(new LockedSlot(mainInventory, k, 8 + k * 18, 98, true, !editable));
        }

        addPlayerInventorySlots();
    }

    @Override
    public void transferItems() {
        if (!isEditable()) {
            return;
        }

        if (!(playerInventory.player instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) playerInventory.player;

        NonNullList<ItemStack> additionalItems = NonNullList.create();
        fill(additionalItems, mainInventory, playerInventory.items);
        fill(additionalItems, armorInventory, playerInventory.armor);
        fill(additionalItems, offHandInventory, playerInventory.offhand);

        additionalItems.addAll(corpse.getDeath().getAdditionalItems());
        NonNullList<ItemStack> restItems = NonNullList.create();
        for (ItemStack stack : additionalItems) {
            if (!player.getInventory().add(stack)) {
                restItems.add(stack);
            }
        }

        corpse.getDeath().getAdditionalItems().clear();
        corpse.getDeath().getAdditionalItems().addAll(restItems);
        if (!corpse.getDeath().getAdditionalItems().isEmpty()) {
            Guis.openAdditionalItems(player, this);
        }
    }

    private void fill(List<ItemStack> additionalItems, Container inventory, NonNullList<ItemStack> playerInv) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack playerStack = playerInv.get(i);
            if (!playerStack.isEmpty()) {
                additionalItems.add(playerStack);
            }
            inventory.setItem(i, ItemStack.EMPTY);
            playerInv.set(i, stack);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (corpse.isMainInventoryEmpty() && !corpse.isAdditionalInventoryEmpty() && playerInventory.player instanceof ServerPlayer) {
            Guis.openAdditionalItems((ServerPlayer) playerInventory.player, this);
        }
    }

    @Override
    public int getInvOffset() {
        return 79;
    }

    @Override
    public int getInventorySize() {
        return 41;
    }
}
