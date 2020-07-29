package de.maxhenkel.corpse;

import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.corelib.CommonRegistry;
import de.maxhenkel.corpse.commands.HistoryCommand;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.CorpseRenderer;
import de.maxhenkel.corpse.events.DeathEvents;
import de.maxhenkel.corpse.events.KeyEvents;
import de.maxhenkel.corpse.gui.CorpseContainer;
import de.maxhenkel.corpse.gui.CorpseContainerFactory;
import de.maxhenkel.corpse.gui.CorpseScreen;
import de.maxhenkel.corpse.net.MessageOpenHistory;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import de.maxhenkel.corpse.net.MessageSwitchInventoryPage;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.lwjgl.glfw.GLFW;

@Mod(Main.MODID)
@Mod.EventBusSubscriber
public class Main {

    public static final String MODID = "corpse";

    @OnlyIn(Dist.CLIENT)
    public static KeyBinding KEY_DEATH_HISTORY;

    public static SimpleChannel SIMPLE_CHANNEL;
    public static EntityType<CorpseEntity> CORPSE_ENTITY_TYPE;
    public static ContainerType<CorpseContainer> CONTAINER_TYPE_CORPSE;
    public static ServerConfig SERVER_CONFIG;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(EntityType.class, this::registerEntities);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainers);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        HistoryCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DeathEvents());

        SIMPLE_CHANNEL = CommonRegistry.registerChannel(Main.MODID, "default");
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 0, MessageSwitchInventoryPage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 1, MessageOpenHistory.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 2, MessageShowCorpseInventory.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 3, MessageRequestDeathHistory.class);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.<CorpseContainer, CorpseScreen>registerScreen(Main.CONTAINER_TYPE_CORPSE, (container, inv, title) -> new CorpseScreen(container.getCorpse(), inv, container, title));

        KEY_DEATH_HISTORY = ClientRegistry.registerKeyBinding("key.corpse.death_history", "key.categories.misc", GLFW.GLFW_KEY_U);
        MinecraftForge.EVENT_BUS.register(new KeyEvents());

        RenderingRegistry.registerEntityRenderingHandler(CORPSE_ENTITY_TYPE, CorpseRenderer::new);
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        CORPSE_ENTITY_TYPE = CommonRegistry.registerEntity(Main.MODID, "corpse", EntityClassification.MISC, CorpseEntity.class, corpseEntityBuilder -> {
            corpseEntityBuilder
                    .setTrackingRange(128)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .size(2F, 0.5F)
                    .setCustomClientFactory((spawnEntity, world) -> new CorpseEntity(world));
        });
        event.getRegistry().register(CORPSE_ENTITY_TYPE);
    }

    @SubscribeEvent
    public void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        CONTAINER_TYPE_CORPSE = new ContainerType<>(new CorpseContainerFactory());
        CONTAINER_TYPE_CORPSE.setRegistryName(new ResourceLocation(Main.MODID, "corpse"));
        event.getRegistry().register(CONTAINER_TYPE_CORPSE);
    }
}
