package de.maxhenkel.corpse.integration.waila;

import de.maxhenkel.corpse.entities.CorpseEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PluginCorpse implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(HUDHandlerCorpse.INSTANCE, CorpseEntity.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(DataProviderCorpse.INSTANCE, CorpseEntity.class);
    }

}