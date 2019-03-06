package de.maxhenkel.corpse;

import de.maxhenkel.corpse.commands.HistoryCommand;
import de.maxhenkel.corpse.entities.EntityCorpse;
import de.maxhenkel.corpse.entities.RenderCorpse;
import de.maxhenkel.corpse.events.KeyEvents;
import de.maxhenkel.corpse.events.DeathEvents;
import de.maxhenkel.corpse.gui.GUIManager;
import de.maxhenkel.corpse.net.MessageOpenHistory;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import de.maxhenkel.corpse.net.MessageSwitchInventoryPage;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
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

@Mod(Main.MODID)
@Mod.EventBusSubscriber
public class Main {

    public static final String MODID = "corpse";

    private static Main instance;

    @OnlyIn(Dist.CLIENT)
    public static KeyBinding KEY_DEATH_HISTORY;

    public static SimpleChannel SIMPLE_CHANNEL;

    public static final EntityType<EntityCorpse> CORPSE_ENTITY_TYPE = EntityType.register(MODID + ":corpse", EntityType.Builder.create(EntityCorpse.class, EntityCorpse::new).tracker(128, 1, true));

    public Main() {
        instance = this;

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DeathEvents());

        //FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverLoad);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::configEvent);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);
        });

        SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Main.MODID, "default"), () -> "1.0.0", s -> true, s -> true);
        SIMPLE_CHANNEL.registerMessage(0, MessageSwitchInventoryPage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageSwitchInventoryPage().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(1, MessageOpenHistory.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageOpenHistory().fromBytes(buf), (msg, fun) -> msg.executeClientSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(2, MessageShowCorpseInventory.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageShowCorpseInventory().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(3, MessageRequestDeathHistory.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageRequestDeathHistory().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
    }

    /*@SubscribeEvent
    public void configEvent(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            Config.loadServer();
        } else if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
            Config.loadClient();
        }
    }*/

    @SubscribeEvent
    public void serverLoad(FMLServerStartingEvent event) {
        HistoryCommand.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        /*
        SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Main.MODID, "default"), () -> "1.0.0", s -> true, s -> true);
        SIMPLE_CHANNEL.registerMessage(0, MessageSwitchInventoryPage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageSwitchInventoryPage().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(1, MessageOpenHistory.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageOpenHistory().fromBytes(buf), (msg, fun) -> msg.executeClientSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(2, MessageShowCorpseInventory.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageShowCorpseInventory().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        SIMPLE_CHANNEL.registerMessage(3, MessageRequestDeathHistory.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageRequestDeathHistory().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
        */
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityCorpse.class, manager -> new RenderCorpse(manager));
        GUIManager.clientSetup();
        KEY_DEATH_HISTORY = new KeyBinding("key.death_history", 85, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_DEATH_HISTORY);
        MinecraftForge.EVENT_BUS.register(new KeyEvents());
    }

    /*@SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(

        );
    }*/

    public static Main instance() {
        return instance;
    }
}
