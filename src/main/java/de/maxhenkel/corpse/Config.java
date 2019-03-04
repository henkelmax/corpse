package de.maxhenkel.corpse;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

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

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            onlyOwnerAccess = builder
                    .comment("If only the owner of the corpse can access the inventory")
                    .translation("only_owner_access")
                    .define("only_owner_access", false);
        }
    }

    public static class ClientConfig {
        public ForgeConfigSpec.ConfigValue<List<String>> dimensionNames;
        public ForgeConfigSpec.ConfigValue<String> dateFormat;
        public ForgeConfigSpec.BooleanValue renderSkull;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            dimensionNames = builder
                    .comment("The names of the Dimensions for the Death Note")
                    .translation("dimension_names")
                    .define("dimension_names", Arrays.asList("minecraft:overworld=Overworld", "minecraft:nether=Nether", "minecraft:the_end=The End"));
            dateFormat = builder
                    .comment("The date format outputted by clicking the gravestone or displayed in the death note")
                    .translation("enable_death_note")
                    .define("enable_death_note", "yyyy/MM/dd HH:mm:ss");
            renderSkull = builder
                    .comment("If this is set to true the players head will be rendered on the gravestone when there is a full block under it")
                    .translation("render_skull")
                    .define("render_skull", true);
        }
    }

}
