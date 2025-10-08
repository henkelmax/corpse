package de.maxhenkel.corpse.gui;

import de.maxhenkel.corelib.inventory.ItemListInventory;
import de.maxhenkel.corelib.inventory.LockedSlot;
import de.maxhenkel.corpse.CorpseMod;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class CorpseAdditionalContainer extends CorpseContainerBase implements ITransferrable {

    private final PlayerInventoryWrapper playerWrapper;

    public CorpseAdditionalContainer(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
        super(CorpseMod.CONTAINER_TYPE_CORPSE_ADDITIONAL_ITEMS.get(), id, playerInventory, corpse, editable, history);
        this.inventory = new ItemListInventory(corpse.getDeath().getAdditionalItems());
        this.playerWrapper = PlayerInventoryWrapper.of(playerInventory);

        setSlots(0);
    }

    public void setSlots(int start) {
        slots.clear();
        for (int j = 0; j < 6; j++) {
            for (int k = 0; k < 9; k++) {
                int index = start + k + j * 9;
                if (index < inventory.getContainerSize()) {
                    addSlot(new LockedSlot(inventory, index, 8 + k * 18, 19 + j * 18, true, !editable));
                } else {
                    addSlot(new LockedSlot(new SimpleContainer(1), 0, 8 + k * 18, 19 + j * 18, true, true));
                }
            }
        }

        addPlayerInventorySlots();
        broadcastChanges();
    }

    @Override
    public void transferItems() {
        if (!isEditable()) {
            return;
        }
        try (Transaction transaction = Transaction.open(null)) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                int inserted = playerWrapper.insert(ItemResource.of(stack), stack.getCount(), transaction);
                int invAmount = stack.getCount() - inserted;
                if (invAmount <= 0) {
                    inventory.setItem(i, ItemStack.EMPTY);
                } else {
                    inventory.setItem(i, stack.split(invAmount));
                }
            }
            transaction.commit();
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
