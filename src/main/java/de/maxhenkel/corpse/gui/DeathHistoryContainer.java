package de.maxhenkel.corpse.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class DeathHistoryContainer extends AbstractContainerMenu {

    public DeathHistoryContainer() {
        super(null, 0);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }
}
