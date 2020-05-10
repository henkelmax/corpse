package de.maxhenkel.corpse.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new DeathHistoryContainerHandler());
    }
}
