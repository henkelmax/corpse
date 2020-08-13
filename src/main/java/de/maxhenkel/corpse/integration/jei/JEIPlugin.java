package de.maxhenkel.corpse.integration.jei;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.util.ResourceLocation;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Main.MODID, "corpse");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(DeathHistoryScreen.class, new DeathHistoryContainerHandler());
    }

}