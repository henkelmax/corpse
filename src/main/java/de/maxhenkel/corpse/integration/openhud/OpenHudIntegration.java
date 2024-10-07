package de.maxhenkel.corpse.integration.openhud;

import de.maxhenkel.openhud.api.OpenHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public class OpenHudIntegration {

    private static final String OPENHUD_MODID = "openhud";
    private static final ResourceLocation SKULL_ICON = ResourceLocation.fromNamespaceAndPath(OPENHUD_MODID, "skull");

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(OPENHUD_MODID);
        }
        return loaded;
    }

    public static void openWaypointScreen(Screen screen, ResourceKey<Level> dimension, BlockPos pos) {
        Screen waypointScreen = OpenHud.getClientUtils().createWaypointScreen(screen, dimension, builder -> builder.icon(SKULL_ICON).position(pos).name(Component.translatable("waypoint.death.name")));
        Minecraft.getInstance().setScreen(waypointScreen);
    }

}
