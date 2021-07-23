package de.maxhenkel.corpse.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class CorpseContainerBase extends ContainerBase {

    protected CorpseEntity corpse;
    protected Inventory playerInventory;
    protected boolean editable, history;

    public CorpseContainerBase(MenuType type, int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
        super(type, id, playerInventory, null);
        this.playerInventory = playerInventory;
        this.corpse = corpse;
        this.editable = editable;
        this.history = history;
    }

    public CorpseEntity getCorpse() {
        return corpse;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isHistory() {
        return history;
    }

    @Override
    public boolean stillValid(Player player) {
        if (history) {
            return true;
        }
        return corpse.distanceTo(player) < 8F && corpse.isAlive();
    }

}
