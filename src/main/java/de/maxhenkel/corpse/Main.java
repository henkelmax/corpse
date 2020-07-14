package de.maxhenkel.corpse;

import de.maxhenkel.corpse.commands.HistoryCommand;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.CorpseRenderer;
import de.maxhenkel.corpse.events.KeyEvents;
import de.maxhenkel.corpse.events.DeathEvents;
import de.maxhenkel.corpse.gui.CorpseContainerFactory;
import de.maxhenkel.corpse.gui.ScreenManager;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
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

    public static ContainerType CONTAINER_TYPE_CORPSE;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(EntityType.class, this::registerEntities);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainers);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup));
    }

    @SubscribeEvent
    public void serverLoad(FMLServerStartingEvent event) {
        HistoryCommand.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DeathEvents());

        SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Main.MODID, "default"), () -> "1.0.0", s -> true, s -> true);
        SIMPLE_CHANNEL.registerMessage(0, MessageSwitchInventoryPage.class, MessageSwitchInventoryPage::toBytes, (buf) -> new MessageSwitchInventoryPage().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(1, MessageOpenHistory.class, MessageOpenHistory::toBytes, (buf) -> new MessageOpenHistory().fromBytes(buf), (msg, fun) -> msg.executeClientSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(2, MessageShowCorpseInventory.class, MessageShowCorpseInventory::toBytes, (buf) -> new MessageShowCorpseInventory().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(3, MessageRequestDeathHistory.class, MessageRequestDeathHistory::toBytes, (buf) -> new MessageRequestDeathHistory().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.clientSetup();
        KEY_DEATH_HISTORY = new KeyBinding("key.corpse.death_history", GLFW.GLFW_KEY_U, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_DEATH_HISTORY);
        MinecraftForge.EVENT_BUS.register(new KeyEvents());

        RenderingRegistry.registerEntityRenderingHandler(CORPSE_ENTITY_TYPE, CorpseRenderer::new);
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        CORPSE_ENTITY_TYPE = EntityType.Builder.<CorpseEntity>create(CorpseEntity::new, EntityClassification.MISC)
                .setTrackingRange(128)
                .setUpdateInterval(1)
                .setShouldReceiveVelocityUpdates(true)
                .size(2F, 0.5F)
                .setCustomClientFactory((spawnEntity, world) -> new CorpseEntity(world))
                .build(Main.MODID + ":corpse");
        CORPSE_ENTITY_TYPE.setRegistryName(new ResourceLocation(Main.MODID, "corpse"));
        event.getRegistry().register(CORPSE_ENTITY_TYPE);
    }

    @SubscribeEvent
    public void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        CONTAINER_TYPE_CORPSE = new ContainerType(new CorpseContainerFactory());
        CONTAINER_TYPE_CORPSE.setRegistryName(new ResourceLocation(Main.MODID, "corpse"));
        event.getRegistry().register(CONTAINER_TYPE_CORPSE);
    }
}
