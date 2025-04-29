package io.github.chakyl.splendidslimes;

import java.util.Optional;
import java.util.function.Supplier;

import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.network.MessageProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;

public class SlimyConfig {

    public static int slimeStarvingTime;
    public static int slimeMaxHappiness;

    public static int slimeHappyThreshold;
    public static int slimeUnhappyThreshold;
    public static int slimeFuriousThreshold;

    public static int slimeEffectCooldown;

    public static boolean enableTarrs;

    public static void load() {
        Configuration cfg = new Configuration(SplendidSlimes.MODID);
        cfg.setTitle("꒷꒦꒷꒦꒷꒦꒷꒦꒷꒦꒷꒦ Splendid Slimes Config! ꒦꒷꒦꒷꒦꒷꒦꒷꒦꒷꒦꒷");
        cfg.setComment("All entries in this config file are synced from server to client.");
        slimeStarvingTime = cfg.getInt("Slime Starving time", "slimes", 24000, 20, Integer.MAX_VALUE, "How long it takes for Splendid Slimes to start starving, in ticks. Slimes can eat halfway through this duration");

        slimeMaxHappiness = cfg.getInt("Slime Max Happiness", "slimes", 1000, 3, Integer.MAX_VALUE - 500, "Maximum happiness value for a Splendid Slime.");
        slimeHappyThreshold = cfg.getInt("Slime Happy Threshold", "slimes", 600, 0, Integer.MAX_VALUE, "Minimum amount of happiness a Splendid Slime can have before being considered 'Happy'.");
        slimeUnhappyThreshold = cfg.getInt("Slime Unhappy Threshold", "slimes", 400, 0, Integer.MAX_VALUE, "Maximum amount of happiness a Splendid Slime can have before being considered 'Unhappy'.");
        slimeFuriousThreshold = cfg.getInt("Slime Furious Threshold", "slimes", 200, 0, Integer.MAX_VALUE, "Maximum amount of happiness a Splendid Slime can have before being considered 'Furious'.");

        slimeEffectCooldown = cfg.getInt("Slime Effect Cooldown", "slimes", 800, 20, Integer.MAX_VALUE, "How many ticks it takes for a Splendid Slime to try and perform Negative/Positive effects/commands. Setting this value too low may cause performance issues.");

        enableTarrs = cfg.getBoolean("Enable Tarrs", "slimes", true, "If true, Largo Slimes will turn into Tarrs after eating a 3rd Plort instead of just dying.");

        if (cfg.hasChanged()) cfg.save();
    }

    static record ConfigMessage(int slimeStarvingTime, int slimeMaxHappiness, int slimeUnhappyThreshold, int slimeFuriousThreshold, int slimeEffectCooldown,  boolean enableTarrs) {

        public ConfigMessage() {
            this(SlimyConfig.slimeStarvingTime, SlimyConfig.slimeMaxHappiness, SlimyConfig.slimeUnhappyThreshold, SlimyConfig.slimeFuriousThreshold, SlimyConfig.slimeEffectCooldown, SlimyConfig.enableTarrs);
        }

        public static class Provider implements MessageProvider<ConfigMessage> {

            @Override
            public Class<?> getMsgClass() {
                return ConfigMessage.class;
            }

            @Override
            public void write(ConfigMessage msg, FriendlyByteBuf buf) {
                buf.writeInt(msg.slimeStarvingTime);
                buf.writeInt(msg.slimeMaxHappiness);
                buf.writeInt(msg.slimeUnhappyThreshold);
                buf.writeInt(msg.slimeFuriousThreshold);
                buf.writeInt(msg.slimeEffectCooldown);
                buf.writeBoolean(msg.enableTarrs);
            }

            @Override
            public ConfigMessage read(FriendlyByteBuf buf) {
                return new ConfigMessage(buf.readInt(), buf.readInt(), buf.readInt(),  buf.readInt(), buf.readInt(), buf.readBoolean());
            }

            @Override
            public void handle(ConfigMessage msg, Supplier<Context> ctx) {
                MessageHelper.handlePacket(() -> {
                    SlimyConfig.slimeStarvingTime = msg.slimeStarvingTime;
                    SlimyConfig.slimeMaxHappiness = msg.slimeMaxHappiness;
                    SlimyConfig.slimeUnhappyThreshold = msg.slimeUnhappyThreshold;
                    SlimyConfig.slimeFuriousThreshold = msg.slimeFuriousThreshold;
                    SlimyConfig.slimeEffectCooldown = msg.slimeEffectCooldown;
                    SlimyConfig.enableTarrs = msg.enableTarrs;
                }, ctx);
            }

            @Override
            public Optional<NetworkDirection> getNetworkDirection() {
                return Optional.of(NetworkDirection.PLAY_TO_CLIENT);
            }

        }

    }

}