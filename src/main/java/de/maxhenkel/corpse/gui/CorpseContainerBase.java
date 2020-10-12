package de.maxhenkel.corpse.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

public class CorpseContainerBase extends ContainerBase {

    protected CorpseEntity corpse;
    protected PlayerInventory playerInventory;
    protected boolean editable, history;

    public CorpseContainerBase(ContainerType type, int id, PlayerInventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
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
    public boolean canInteractWith(PlayerEntity player) {
        if (history) {
            return true;
        }
        return corpse.getDistance(player) < 8F && corpse.isAlive();
    }

}
