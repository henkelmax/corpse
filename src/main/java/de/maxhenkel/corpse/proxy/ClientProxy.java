package de.maxhenkel.corpse.proxy;

import de.maxhenkel.corpse.entities.EntityCorpse;
import de.maxhenkel.corpse.entities.RenderFactoryCorpse;
import de.maxhenkel.corpse.events.KeyEvents;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static KeyBinding KEY_DEATH_HISTORY;

    public void preinit(FMLPreInitializationEvent event) {
        super.preinit(event);

        RenderingRegistry.registerEntityRenderingHandler(EntityCorpse.class, new RenderFactoryCorpse());
        KEY_DEATH_HISTORY = new KeyBinding("key.death_history", Keyboard.KEY_U, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_DEATH_HISTORY);
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new KeyEvents());

    }

    public void postinit(FMLPostInitializationEvent event) {
        super.postinit(event);
    }

}
