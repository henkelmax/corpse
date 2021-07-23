package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;

public class CorpseContainerProvider implements MenuProvider {

    private CorpseEntity corpse;
    private boolean editable, history;

    public CorpseContainerProvider(CorpseEntity corpse, boolean editable, boolean history) {
        this.corpse = corpse;
        this.editable = editable;
        this.history = history;
    }

    @Override
    public Component getDisplayName() {
        return corpse.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new CorpseInventoryContainer(id, playerInventory, corpse, editable, history);
    }
}