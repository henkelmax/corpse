package de.maxhenkel.corpse;

import de.maxhenkel.corelib.ClientRegistry;
import de.maxhenkel.corelib.CommonRegistry;
import de.maxhenkel.corelib.dataserializers.DataSerializerItemList;
import de.maxhenkel.corpse.commands.HistoryCommand;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.CorpseRenderer;
import de.maxhenkel.corpse.events.DeathEvents;
import de.maxhenkel.corpse.events.KeyEvents;
import de.maxhenkel.corpse.gui.*;
import de.maxhenkel.corpse.net.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(Main.MODID)
@Mod.EventBusSubscriber
public class Main {

    public static final String MODID = "corpse";

    public static final Logger LOGGER = LogManager.getLogger(Main.MODID);

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_DEATH_HISTORY;

    public static SimpleChannel SIMPLE_CHANNEL;

    private static final DeferredRegister<EntityType<?>> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, Main.MODID);
    public static final RegistryObject<EntityType<CorpseEntity>> CORPSE_ENTITY_TYPE = ITEM_REGISTER.register("corpse", Main::createCorpseEntityType);

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, Main.MODID);
    public static final RegistryObject<MenuType<CorpseAdditionalContainer>> CONTAINER_TYPE_CORPSE_ADDITIONAL_ITEMS = MENU_REGISTER.register("corpse_additional_items", Main::createCorpseAdditionalItemsMenuType);
    public static final RegistryObject<MenuType<CorpseInventoryContainer>> CONTAINER_TYPE_CORPSE_INVENTORY = MENU_REGISTER.register("corpse_inventory", Main::createCorpseInventoryMenuType);

    private static final DeferredRegister<EntityDataSerializer<?>> DATA_SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.DATA_SERIALIZERS, Main.MODID);
    public static final RegistryObject<EntityDataSerializer<NonNullList<ItemStack>>> ITEM_LIST_SERIALIZER = DATA_SERIALIZER_REGISTER.register("item_list", () -> DataSerializerItemList.create());

    public static ServerConfig SERVER_CONFIG;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup));

        ITEM_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        MENU_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        DATA_SERIALIZER_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
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
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 4, MessageTransferItems.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 5, MessageOpenAdditionalItems.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 6, MessageSpawnDeathParticles.class);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.<CorpseAdditionalContainer, CorpseAdditionalScreen>registerScreen(Main.CONTAINER_TYPE_CORPSE_ADDITIONAL_ITEMS.get(), (container, inv, title) -> new CorpseAdditionalScreen(container.getCorpse(), inv, container, title));
        ClientRegistry.<CorpseInventoryContainer, CorpseInventoryScreen>registerScreen(Main.CONTAINER_TYPE_CORPSE_INVENTORY.get(), (container, inv, title) -> new CorpseInventoryScreen(container.getCorpse(), inv, container, title));

        KEY_DEATH_HISTORY = ClientRegistry.registerKeyBinding("key.corpse.death_history", "key.categories.misc", GLFW.GLFW_KEY_U);
        MinecraftForge.EVENT_BUS.register(new KeyEvents());

        // TODO Fix
        // RenderingRegistry.registerEntityRenderingHandler(CORPSE_ENTITY_TYPE, CorpseRenderer::new);
        EntityRenderers.register(CORPSE_ENTITY_TYPE.get(), CorpseRenderer::new);
    }

    private static EntityType<CorpseEntity> createCorpseEntityType() {
        return CommonRegistry.registerEntity(Main.MODID, "corpse", MobCategory.MISC, CorpseEntity.class, corpseEntityBuilder -> {
            corpseEntityBuilder
                    .setTrackingRange(128)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(2F, 0.5F)
                    .setCustomClientFactory((spawnEntity, world) -> new CorpseEntity(world));
        });
    }

    private static MenuType<CorpseAdditionalContainer> createCorpseAdditionalItemsMenuType() {
        return new MenuType<>(new CorpseContainerFactory<CorpseAdditionalContainer>() {
            @Override
            public CorpseAdditionalContainer create(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
                return new CorpseAdditionalContainer(id, playerInventory, corpse, editable, history);
            }
        });
    }

    private static MenuType<CorpseInventoryContainer> createCorpseInventoryMenuType() {
        return new MenuType<>(new CorpseContainerFactory<CorpseInventoryContainer>() {
            @Override
            public CorpseInventoryContainer create(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
                return new CorpseInventoryContainer(id, playerInventory, corpse, editable, history);
            }
        });
    }

}
