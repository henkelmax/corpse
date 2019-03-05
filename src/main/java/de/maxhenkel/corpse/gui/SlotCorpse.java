package de.maxhenkel.corpse.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCorpse extends Slot {

    private boolean editable;

    public SlotCorpse(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean editable) {
        super(inventoryIn, index, xPosition, yPosition);
        this.editable = editable;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return editable ? super.canTakeStack(playerIn) : false;
    }
}
