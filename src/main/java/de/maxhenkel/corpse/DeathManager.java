package de.maxhenkel.corpse;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class DeathManager {

    public static void addDeath(ServerPlayerEntity player, Death death) {
        try {
            File deathFile = getDeathFile(player, death.getId());
            deathFile.getParentFile().mkdirs();
            CompressedStreamTools.write(death.toNBT(), deathFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static Death getDeath(ServerPlayerEntity player, UUID id) {
        try {
            return Death.fromNBT(CompressedStreamTools.read(getDeathFile(player, id)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Death> getDeaths(ServerPlayerEntity player) {
        return getDeaths(player);
    }

    public static List<Death> getDeaths(ServerPlayerEntity context, ServerPlayerEntity player) {
        return getDeaths(context, player);
    }

    public static List<Death> getDeaths(ServerPlayerEntity context, UUID playerUUID) {
        File playerDeathFolder = getPlayerDeathFolder(context, playerUUID);

        if (!playerDeathFolder.exists()) {
            return Collections.emptyList();
        }

        File[] deaths = playerDeathFolder.listFiles((dir, name) -> {
            String[] split = name.split("\\.");
            if (split.length != 2) {
                return false;
            }
            if (split[1].equals("dat")) {
                try {
                    UUID.fromString(split[0]);
                    return true;
                } catch (Exception e) {
                }
            }
            return false;
        });

        return Arrays.stream(deaths)
                .map(f -> {
                    try {
                        return Death.fromNBT(CompressedStreamTools.read(f));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(d -> d != null)
                .sorted(Comparator.comparingLong(Death::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public static File getDeathFile(ServerPlayerEntity player, UUID id) {
        return new File(getPlayerDeathFolder(player), id.toString() + ".dat");
    }

    public static File getPlayerDeathFolder(ServerPlayerEntity player) {
        return getPlayerDeathFolder(player, player.getUniqueID());
    }

    public static File getPlayerDeathFolder(ServerPlayerEntity context, UUID uuid) {
        return new File(getDeathFolder(context.getServerWorld()), uuid.toString());
    }

    public static File getDeathFolder(ServerWorld world) {
        return new File(world.getSaveHandler().getWorldDirectory(), "deaths");
    }
}
