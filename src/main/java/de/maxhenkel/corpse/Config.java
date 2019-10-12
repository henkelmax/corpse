package de.maxhenkel.corpse;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<ServerConfig, ForgeConfigSpec> specPairServer = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPairServer.getRight();
        SERVER = specPairServer.getLeft();

        Pair<ClientConfig, ForgeConfigSpec> specPairClient = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPairClient.getRight();
        CLIENT = specPairClient.getLeft();
    }

    public static class ServerConfig {
        public ForgeConfigSpec.BooleanValue onlyOwnerAccess;
        public ForgeConfigSpec.BooleanValue skeletonAccess;
        public ForgeConfigSpec.IntValue corpseDespawnTime;
        public ForgeConfigSpec.IntValue corpseForceDespawnTime;
        public ForgeConfigSpec.IntValue corpseSkeletonTime;
        public ForgeConfigSpec.BooleanValue spawnCorpseOnFace;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            onlyOwnerAccess = builder
                    .comment("If only the owner of the corpse can access the inventory")
                    .define("only_owner_access", false);
            skeletonAccess = builder
                    .comment("If everybody can access the corpse in the skeleton stage (Only if only_owner_access is set to true)")
                    .define("skeleton_access", false);
            corpseDespawnTime = builder
                    .comment("The time passed after a corpse despawns (only if empty)")
                    .defineInRange("corpse_despawn_time", 20 * 30, 20, Integer.MAX_VALUE);
            corpseForceDespawnTime = builder
                    .comment("The time passed after a corpse despawns even if its not empty (-1 = never)")
                    .defineInRange("corpse_force_despawn_time", -1, -1, Integer.MAX_VALUE);
            corpseSkeletonTime = builder
                    .comment("The time passed after a corpse turns into a skeleton")
                    .defineInRange("corpse_skeleton_time", 20 * 60 * 60, 0, Integer.MAX_VALUE);
            spawnCorpseOnFace = builder
                    .comment("If the corpse should spawn on its face")
                    .define("spawn_corpse_on_face", false);
        }
    }

    public static class ClientConfig {

        public ClientConfig(ForgeConfigSpec.Builder builder) {

        }
    }

}
