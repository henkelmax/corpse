package de.maxhenkel.corpse.integration.jei;

import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.Arrays;

public class DeathHistoryContainerHandler implements IGuiContainerHandler<DeathHistoryScreen> {

    @Override
    public java.util.List<Rectangle2d> getGuiExtraAreas(DeathHistoryScreen containerScreen) {
        return Arrays.asList(new Rectangle2d(0, 0, containerScreen.field_230708_k_, containerScreen.field_230709_l_));
    }
}