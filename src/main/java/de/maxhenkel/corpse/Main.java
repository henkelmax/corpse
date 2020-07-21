package de.maxhenkel.corpse;

import de.maxhenkel.corpse.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Main.MODID, version = "1.12.2-1.0.8", acceptedMinecraftVersions = "[1.12.2]", updateJSON = "https://maxhenkel.de/update/corpse.json", dependencies = "")
public class Main {

    public static final String MODID = "corpse";

    @Mod.Instance
    private static Main instance;

    @SidedProxy(clientSide = "de.maxhenkel.corpse.proxy.ClientProxy", serverSide = "de.maxhenkel.corpse.proxy.CommonProxy")
    public static CommonProxy proxy;

    public Main() {
        instance = this;
    }

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        proxy.preinit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postinit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }


    public static Main instance() {
        return instance;
    }
}
