package de.maxhenkel.corpse.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class CorpseSlot extends Slot {

    private boolean editable;

    public CorpseSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean editable) {
        super(inventoryIn, index, xPosition, yPosition);
        this.editable = editable;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return editable ? super.canTakeStack(playerIn) : false;
    }
}
