package io.github.chakyl.splendidslimes;

import dev.shadowsoffire.placebo.network.PacketDistro;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = SplendidSlimes.MODID)
public class ConfigEvents {

    @SubscribeEvent
    public static void reload(AddReloadListenerEvent e) {
        e.addListener((ResourceManagerReloadListener) resman -> SlimyConfig.load());
    }

    @SubscribeEvent
    public static void sync(OnDatapackSyncEvent e) {
        if (e.getPlayer() != null) {
            PacketDistro.sendTo(SplendidSlimes.CHANNEL, new SlimyConfig.ConfigMessage(), e.getPlayer());
        }
        else {
            PacketDistro.sendToAll(SplendidSlimes.CHANNEL, new SlimyConfig.ConfigMessage());
        }
    }
}