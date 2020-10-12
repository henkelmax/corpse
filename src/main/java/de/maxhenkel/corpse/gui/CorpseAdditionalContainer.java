package de.maxhenkel.corpse.gui;

import de.maxhenkel.corelib.inventory.ItemListInventory;
import de.maxhenkel.corelib.inventory.LockedSlot;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class CorpseAdditionalContainer extends CorpseContainerBase implements ITransferrable {

    private PlayerMainInvWrapper playerWrapper;

    public CorpseAdditionalContainer(int id, PlayerInventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
        super(Main.CONTAINER_TYPE_CORPSE_ADDITIONAL_ITEMS, id, playerInventory, corpse, editable, history);
        this.inventory = new ItemListInventory(corpse.getDeath().getAdditionalItems());
        this.playerWrapper = new PlayerMainInvWrapper(playerInventory);

        setSlots(0);
    }

    public void setSlots(int start) {
        inventorySlots.clear();
        for (int j = 0; j < 6; j++) {
            for (int k = 0; k < 9; k++) {
                int index = start + k + j * 9;
                if (index < inventory.getSizeInventory()) {
                    addSlot(new LockedSlot(inventory, index, 8 + k * 18, 19 + j * 18, true, !editable));
                } else {
                    addSlot(new LockedSlot(new Inventory(1), 0, 8 + k * 18, 19 + j * 18, true, true));
                }
            }
        }

        addPlayerInventorySlots();
        detectAndSendChanges();
    }

    @Override
    public void transferItems() {
        if (!isEditable()) {
            return;
        }
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            for (int j = 0; j < playerWrapper.getSlots(); j++) {
                stack = playerWrapper.insertItem(j, stack, false);
                inventory.setInventorySlotContents(i, stack);
                if (stack.isEmpty()) {
                    break;
                }
            }
        }
    }

    @Override
    public int getInvOffset() {
        return 82;
    }

    @Override
    public int getInventorySize() {
        return 54;
    }
}
