package de.maxhenkel.corpse;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class DeathManager {

    public static void addDeath(EntityPlayerMP player, Death death) {
        try {
            File deathFile = getDeathFile(player, death.getId());
            deathFile.getParentFile().mkdirs();
            CompressedStreamTools.write(death.toNBT(), deathFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static Death getDeath(EntityPlayerMP player, UUID id) {
        try {
            return Death.fromNBT(CompressedStreamTools.read(getDeathFile(player, id)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Death> getDeaths(EntityPlayerMP player) {
        File playerDeathFolder = getPlayerDeathFolder(player);

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

    public static File getDeathFile(EntityPlayerMP player, UUID id) {
        return new File(getPlayerDeathFolder(player), id.toString() + ".dat");
    }

    public static File getPlayerDeathFolder(EntityPlayerMP player) {
        return new File(getDeathFolder(player.getServerWorld()), player.getUniqueID().toString());
    }

    public static File getDeathFolder(WorldServer world) {
        return new File(world.getSaveHandler().getWorldDirectory(), "deaths");
    }

}
