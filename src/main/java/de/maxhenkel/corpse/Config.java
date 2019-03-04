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

    public static SimpleDateFormat imageDateFormat=new SimpleDateFormat("MM/dd/yyyy HH:mm");
    public static long imageCooldown=5000;

    public static void loadServer() {
        imageDateFormat=new SimpleDateFormat(SERVER.imageDateFormat.get());
        imageCooldown=SERVER.imageCooldown.get();
    }

    public static void loadClient(){

    }

    public static class ServerConfig {
        public ForgeConfigSpec.LongValue imageCooldown;
        public ForgeConfigSpec.ConfigValue<String> imageDateFormat;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            imageCooldown = builder
                    .comment("The time in milliseconds the CAMERA will be on cooldown after taking an IMAGE")
                    .translation("image_cooldown")
                    .defineInRange("image_cooldown", 5000L, 100L, Integer.MAX_VALUE);
            imageDateFormat = builder
                    .comment("The format the date will be displayed on the IMAGE")
                    .translation("image_date_format")
                    .define("image_date_format", "MM/dd/yyyy HH:mm");
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
