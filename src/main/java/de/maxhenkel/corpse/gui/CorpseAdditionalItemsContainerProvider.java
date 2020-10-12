package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class CorpseAdditionalItemsContainerProvider implements INamedContainerProvider {

    private CorpseEntity corpse;
    private boolean editable, history;

    public CorpseAdditionalItemsContainerProvider(CorpseEntity corpse, boolean editable, boolean history) {
        this.corpse = corpse;
        this.editable = editable;
        this.history = history;
    }

    @Override
    public ITextComponent getDisplayName() {
        return corpse.getDisplayName();
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new CorpseAdditionalContainer(id, playerInventory, corpse, editable, history);
    }
}