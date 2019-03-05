package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.EntityCorpse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;

import javax.annotation.Nullable;

public class InteractionObjectCorpse implements IInteractionObject {

    private EntityCorpse corpse;
    private boolean editable;

    public InteractionObjectCorpse(EntityCorpse corpse, boolean editable) {
        this.corpse = corpse;
        this.editable = editable;
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerCorpse(playerInventory, corpse, editable);
    }

    @Override
    public String getGuiID() {
        return Main.MODID + ":corpse";
    }

    @Override
    public ITextComponent getName() {
        return corpse.getDisplayName();
    }

    @Override
    public boolean hasCustomName() {
        return corpse.hasCustomName();
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return corpse.getCustomName();
    }
}