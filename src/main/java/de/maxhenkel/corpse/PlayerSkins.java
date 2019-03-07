package de.maxhenkel.corpse;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSkins {

    private static HashMap<UUID, GameProfile> players = new HashMap();

    public static ResourceLocation getSkin(UUID uuid, String name) {
        GameProfile profile = getGameProfile(uuid, name);

        Minecraft minecraft = Minecraft.getMinecraft();
        Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);

        if (map.containsKey(Type.SKIN)) {
            return minecraft.getSkinManager().loadSkin(map.get(Type.SKIN), Type.SKIN);
        } else {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
    }

    /*public static boolean isAvailable(GameProfile gameProfile) {
        return gameProfile.isComplete() && gameProfile.getProperties().containsKey("textures");
    }*/

    public static GameProfile getGameProfile(UUID uuid, String name) {
        if (players.containsKey(uuid)) {
            return players.get(uuid);
        } else {
            GameProfile profile = TileEntitySkull.updateGameprofile(new GameProfile(uuid, name));
            players.put(uuid, profile);
            return profile;
        }
    }

}
