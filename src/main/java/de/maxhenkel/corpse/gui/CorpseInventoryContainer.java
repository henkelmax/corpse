package de.maxhenkel.corpse.gui;

import com.mojang.datafixers.util.Pair;
import de.maxhenkel.corelib.inventory.ItemListInventory;
import de.maxhenkel.corelib.inventory.LockedSlot;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class CorpseInventoryContainer extends CorpseContainerBase implements ITransferrable {

    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET};

    private ItemListInventory mainInventory;
    private ItemListInventory armorInventory;
    private ItemListInventory offHandInventory;

    public CorpseInventoryContainer(int id, PlayerInventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
        super(Main.CONTAINER_TYPE_CORPSE_INVENTORY, id, playerInventory, corpse, editable, history);

        mainInventory = new ItemListInventory(corpse.getDeath().getMainInventory());
        armorInventory = new ItemListInventory(corpse.getDeath().getArmorInventory());
        offHandInventory = new ItemListInventory(corpse.getDeath().getOffHandInventory());

        for (int i = 0; i < 4; i++) {
            int slotIndex = 3 - i;
            addSlot(new LockedSlot(armorInventory, slotIndex, 8 + i * 18, 18, true, !editable) {
                @OnlyIn(Dist.CLIENT)
                @Override
                public Pair<ResourceLocation, ResourceLocation> getBackground() {
                    return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, ARMOR_SLOT_TEXTURES[slotIndex]);
                }
            });
        }

        addSlot(new LockedSlot(offHandInventory, 0, 98, 18, true, !editable) {
            @OnlyIn(Dist.CLIENT)
            @Override
            public Pair<ResourceLocation, ResourceLocation> getBackground() {
                return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
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

        if (!(playerInventory.player instanceof ServerPlayerEntity)) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) playerInventory.player;

        NonNullList<ItemStack> additionalItems = NonNullList.create();
        fill(additionalItems, mainInventory, playerInventory.mainInventory);
        fill(additionalItems, armorInventory, playerInventory.armorInventory);
        fill(additionalItems, offHandInventory, playerInventory.offHandInventory);

        additionalItems.addAll(corpse.getDeath().getAdditionalItems());
        NonNullList<ItemStack> restItems = NonNullList.create();
        for (ItemStack stack : additionalItems) {
            if (!player.inventory.addItemStackToInventory(stack)) {
                restItems.add(stack);
            }
        }

        corpse.getDeath().getAdditionalItems().clear();
        corpse.getDeath().getAdditionalItems().addAll(restItems);
        if (!corpse.getDeath().getAdditionalItems().isEmpty()) {
            Guis.openAdditionalItems(player, this);
        }
    }

    private void fill(List<ItemStack> additionalItems, IInventory inventory, NonNullList<ItemStack> playerInv) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack playerStack = playerInv.get(i);
            if (!playerStack.isEmpty()) {
                additionalItems.add(playerStack);
            }
            inventory.setInventorySlotContents(i, ItemStack.EMPTY);
            playerInv.set(i, stack);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (corpse.isMainInventoryEmpty() && !corpse.isAdditionalInventoryEmpty() && playerInventory.player instanceof ServerPlayerEntity) {
            Guis.openAdditionalItems((ServerPlayerEntity) playerInventory.player, this);
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
