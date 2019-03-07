package de.maxhenkel.corpse.proxy;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.commands.HistoryCommand;
import de.maxhenkel.corpse.entities.EntityCorpse;
import de.maxhenkel.corpse.events.DeathEvents;
import de.maxhenkel.corpse.gui.GuiHandler;
import de.maxhenkel.corpse.net.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommonProxy {

    private static Map<UUID, Death> deathsToShow = new HashMap<>();

    public static SimpleNetworkWrapper simpleNetworkWrapper;

    public static boolean onlyOwnerAccess = false;

    public void preinit(FMLPreInitializationEvent event) {
        try {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            onlyOwnerAccess = config.getBoolean("only_owner_access", "corpse", false, "If only the owner of the corpse can access the inventory");
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        simpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Main.MODID);

        simpleNetworkWrapper.registerMessage(MessageSwitchInventoryPage.class, MessageSwitchInventoryPage.class, 0, Side.SERVER);
        simpleNetworkWrapper.registerMessage(MessageOpenHistory.class, MessageOpenHistory.class, 1, Side.CLIENT);
        simpleNetworkWrapper.registerMessage(MessageShowCorpseInventory.class, MessageShowCorpseInventory.class, 2, Side.SERVER);
        simpleNetworkWrapper.registerMessage(MessageRequestDeathHistory.class, MessageRequestDeathHistory.class, 3, Side.SERVER);
        simpleNetworkWrapper.registerMessage(MessageDeathInventory.class, MessageDeathInventory.class, 4, Side.CLIENT);
        simpleNetworkWrapper.registerMessage(MessageTeleport.class, MessageTeleport.class, 5, Side.SERVER);

    }

    public void init(FMLInitializationEvent event) {
        EntityRegistry.registerModEntity(new ResourceLocation(Main.MODID, "corpse"), EntityCorpse.class,
                "corpse", 3632, Main.instance(), 128, 1, true);

        NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance(), new GuiHandler());

        MinecraftForge.EVENT_BUS.register(new DeathEvents());
    }

    public void postinit(FMLPostInitializationEvent event) {

    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new HistoryCommand());
    }

    public static Death getDeathToShow(EntityPlayer player) {
        return deathsToShow.get(player.getUniqueID());
    }

    public static void setDeathToShow(EntityPlayer player, Death death) {
        deathsToShow.put(player.getUniqueID(), death);
    }

}
