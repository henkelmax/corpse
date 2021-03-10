package de.maxhenkel.corpse.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

public class DeathHistoryContainer extends Container {

    public DeathHistoryContainer() {
        super(null, 0);
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
    }
}
