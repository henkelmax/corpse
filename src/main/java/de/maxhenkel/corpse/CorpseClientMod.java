package de.maxhenkel.corpse;

import de.maxhenkel.corpse.entities.CorpseRenderer;
import de.maxhenkel.corpse.events.KeyEvents;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import de.maxhenkel.corpse.gui.CorpseAdditionalScreen;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import de.maxhenkel.corpse.gui.CorpseInventoryScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = CorpseMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CorpseMod.MODID, value = Dist.CLIENT)
public class CorpseClientMod {

    public static KeyMapping KEY_DEATH_HISTORY;

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new KeyEvents());

        EntityRenderers.register(CorpseMod.CORPSE_ENTITY_TYPE.get(), CorpseRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterScreens(RegisterMenuScreensEvent containers) {
        containers.<CorpseAdditionalContainer, CorpseAdditionalScreen>register(CorpseMod.CONTAINER_TYPE_CORPSE_ADDITIONAL_ITEMS.get(), (container, inv, title) -> new CorpseAdditionalScreen(container.getCorpse(), inv, container, title));
        containers.<CorpseInventoryContainer, CorpseInventoryScreen>register(CorpseMod.CONTAINER_TYPE_CORPSE_INVENTORY.get(), (container, inv, title) -> new CorpseInventoryScreen(container.getCorpse(), inv, container, title));
    }

    @SubscribeEvent
    static void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        KEY_DEATH_HISTORY = new KeyMapping("key.corpse.death_history", GLFW.GLFW_KEY_U, "key.categories.misc");
        event.register(KEY_DEATH_HISTORY);
    }

}
