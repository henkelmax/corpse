package de.maxhenkel.corpse;

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
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "corpse";

    public static final Logger LOGGER = LogManager.getLogger(Main.MODID);

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_DEATH_HISTORY;

    private static final DeferredRegister<EntityType<?>> ITEM_REGISTER = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Main.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<CorpseEntity>> CORPSE_ENTITY_TYPE = ITEM_REGISTER.register("corpse", Main::createCorpseEntityType);

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(BuiltInRegistries.MENU, Main.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<CorpseAdditionalContainer>> CONTAINER_TYPE_CORPSE_ADDITIONAL_ITEMS = MENU_REGISTER.register("corpse_additional_items", Main::createCorpseAdditionalItemsMenuType);
    public static final DeferredHolder<MenuType<?>, MenuType<CorpseInventoryContainer>> CONTAINER_TYPE_CORPSE_INVENTORY = MENU_REGISTER.register("corpse_inventory", Main::createCorpseInventoryMenuType);

    private static final DeferredRegister<EntityDataSerializer<?>> DATA_SERIALIZER_REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Main.MODID);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<NonNullList<ItemStack>>> ITEM_LIST_SERIALIZER = DATA_SERIALIZER_REGISTER.register("item_list", () -> DataSerializerItemList.create());
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<UUID>> UUID_SERIALIZER = DATA_SERIALIZER_REGISTER.register("uuid", () -> EntityDataSerializer.forValueType(UUIDUtil.STREAM_CODEC));


    public static ServerConfig SERVER_CONFIG;

    public Main(IEventBus eventBus) {
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::onRegisterPayloadHandler);
        SERVER_CONFIG = CommonRegistry.registerConfig(MODID, ModConfig.Type.SERVER, ServerConfig.class);
        if (FMLEnvironment.dist.isClient()) {
            eventBus.addListener(Main.this::clientSetup);
            eventBus.addListener(Main.this::onRegisterKeyBinds);
            eventBus.addListener(Main.this::onRegisterScreens);
        }

        ITEM_REGISTER.register(eventBus);
        MENU_REGISTER.register(eventBus);
        DATA_SERIALIZER_REGISTER.register(eventBus);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        HistoryCommand.register(event.getDispatcher());
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new DeathEvents());
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new KeyEvents());

        EntityRenderers.register(CORPSE_ENTITY_TYPE.get(), CorpseRenderer::new);
    }

    @OnlyIn(Dist.CLIENT)
    public void onRegisterScreens(RegisterMenuScreensEvent containers) {
        containers.<CorpseAdditionalContainer, CorpseAdditionalScreen>register(Main.CONTAINER_TYPE_CORPSE_ADDITIONAL_ITEMS.get(), (container, inv, title) -> new CorpseAdditionalScreen(container.getCorpse(), inv, container, title));
        containers.<CorpseInventoryContainer, CorpseInventoryScreen>register(Main.CONTAINER_TYPE_CORPSE_INVENTORY.get(), (container, inv, title) -> new CorpseInventoryScreen(container.getCorpse(), inv, container, title));
    }

    public void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID).versioned("0");
        CommonRegistry.registerMessage(registrar, MessageSwitchInventoryPage.class);
        CommonRegistry.registerMessage(registrar, MessageOpenHistory.class);
        CommonRegistry.registerMessage(registrar, MessageShowCorpseInventory.class);
        CommonRegistry.registerMessage(registrar, MessageRequestDeathHistory.class);
        CommonRegistry.registerMessage(registrar, MessageTransferItems.class);
        CommonRegistry.registerMessage(registrar, MessageOpenAdditionalItems.class);
        CommonRegistry.registerMessage(registrar, MessageSpawnDeathParticles.class);
    }

    @OnlyIn(Dist.CLIENT)
    public void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        KEY_DEATH_HISTORY = new KeyMapping("key.corpse.death_history", GLFW.GLFW_KEY_U, "key.categories.misc");
        event.register(KEY_DEATH_HISTORY);
    }

    private static EntityType<CorpseEntity> createCorpseEntityType() {
        return CommonRegistry.registerEntity(Main.MODID, "corpse", MobCategory.MISC, CorpseEntity.class, corpseEntityBuilder -> {
            corpseEntityBuilder
                    .setTrackingRange(128)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(2F, 0.5F)
                    .eyeHeight(0.25F);
        });
    }

    private static MenuType<CorpseAdditionalContainer> createCorpseAdditionalItemsMenuType() {
        return new MenuType<>(new CorpseContainerFactory<CorpseAdditionalContainer>() {
            @Override
            public CorpseAdditionalContainer create(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
                return new CorpseAdditionalContainer(id, playerInventory, corpse, editable, history);
            }
        }, FeatureFlags.VANILLA_SET);
    }

    private static MenuType<CorpseInventoryContainer> createCorpseInventoryMenuType() {
        return new MenuType<>(new CorpseContainerFactory<CorpseInventoryContainer>() {
            @Override
            public CorpseInventoryContainer create(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history) {
                return new CorpseInventoryContainer(id, playerInventory, corpse, editable, history);
            }
        }, FeatureFlags.VANILLA_SET);
    }

}
