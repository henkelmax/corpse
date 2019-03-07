package de.maxhenkel.corpse.gui;

import com.google.common.collect.Lists;
import de.maxhenkel.corpse.entities.EntityCorpse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.NonNullList;

public class ContainerCorpse extends ContainerBase {

    private EntityCorpse corpse;
    private boolean editable;

    public ContainerCorpse(IInventory playerInventory, EntityCorpse corpse, boolean editable) {
        super(playerInventory, corpse);
        this.corpse = corpse;
        this.editable = editable;

        setSlots(0);
    }

    public void setSlots(int start) {
        inventorySlots = Lists.newArrayList();
        inventoryItemStacks = NonNullList.create();
        for (int j = 0; j < 6; j++) {
            for (int k = 0; k < 9; k++) {
                addSlotToContainer(new SlotCorpse(corpse, start + k + j * 9, 8 + k * 18, 19 + j * 18, editable));
            }
        }

        addInvSlots();
        listeners.stream().forEach(listener -> listener.sendAllContents(this, getInventory()));
    }

    public EntityCorpse getCorpse() {
        return corpse;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
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
