package de.maxhenkel.corpse;

import de.maxhenkel.corpse.entities.EntityCorpse;
import de.maxhenkel.corpse.entities.RenderCorpse;
import de.maxhenkel.corpse.events.DeathEvents;
import de.maxhenkel.corpse.gui.GUICorpse;
import de.maxhenkel.corpse.gui.GUIRegistry;
import de.maxhenkel.corpse.net.MessageSwitchPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.UUID;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "corpse";

    private static Main instance;

    public static SimpleChannel SIMPLE_CHANNEL;

    public static final EntityType<EntityCorpse> CORPSE_ENTITY_TYPE = EntityType.register(MODID + ":corpse", EntityType.Builder.create(EntityCorpse.class, EntityCorpse::new).tracker(128, 1, true));

    public Main() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::configEvent);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);
        });
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
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DeathEvents());

        SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Main.MODID, "default"), () -> "1.0.0", s -> true, s -> true);
        SIMPLE_CHANNEL.registerMessage(0, MessageSwitchPage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new MessageSwitchPage().fromBytes(buf), (msg, fun) -> msg.executeServerSide(fun.get()));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityCorpse.class, manager -> new RenderCorpse(manager));

        GUIRegistry.register(new ResourceLocation(MODID, "corpse"), openContainer -> {
            EntityPlayerSP player = Minecraft.getInstance().player;
            PacketBuffer buffer = openContainer.getAdditionalData();
            UUID uuid = new UUID(buffer.readLong(), buffer.readLong());
            EntityCorpse entity = player.world.getEntities(EntityCorpse.class, corpse -> corpse.getUniqueID().equals(uuid)).stream().findFirst().get();
            return new GUICorpse(player.inventory, entity);
        });
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(

        );
    }

    public static Main instance() {
        return instance;
    }
}
