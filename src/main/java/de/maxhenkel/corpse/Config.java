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
        public ForgeConfigSpec.IntValue corpseDespawnTime;
        public ForgeConfigSpec.IntValue corpseSkeletonTime;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            onlyOwnerAccess = builder
                    .comment("If only the owner of the corpse can access the inventory")
                    .translation("only_owner_access")
                    .define("only_owner_access", false);
            corpseDespawnTime = builder
                    .comment("The time passed after a corpse despawns (only if empty)")
                    .translation("corpse_despawn_time")
                    .defineInRange("corpse_despawn_time", 20 * 30, 20, Integer.MAX_VALUE);
            corpseSkeletonTime = builder
                    .comment("The time passed after a corpse turns into a skeleton")
                    .translation("corpse_skeleton_time")
                    .defineInRange("corpse_skeleton_time", 20 * 60 * 60, 0, Integer.MAX_VALUE);
        }
    }

    public static class ClientConfig {

        public ClientConfig(ForgeConfigSpec.Builder builder) {

        }
    }

}
