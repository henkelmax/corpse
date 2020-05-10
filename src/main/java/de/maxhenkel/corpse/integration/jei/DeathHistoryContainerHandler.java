package de.maxhenkel.corpse.integration.jei;

import de.maxhenkel.corpse.gui.GUIDeathHistory;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class DeathHistoryContainerHandler implements IAdvancedGuiHandler<GUIDeathHistory> {

    @Override
    public Class<GUIDeathHistory> getGuiContainerClass() {
        return GUIDeathHistory.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(GUIDeathHistory guiContainer) {
        return Arrays.asList(new Rectangle(0, 0, guiContainer.width, guiContainer.height));
    }
}
