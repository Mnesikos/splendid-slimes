package io.github.chakyl.splendidslimes.events;

import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.client.Keybindings;
import io.github.chakyl.splendidslimes.network.PacketHandler;
import io.github.chakyl.splendidslimes.network.SlimeVacModePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = SplendidSlimes.MODID)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (Keybindings.INSTANCE.slimeVacModeKey.consumeClick()) {
            PacketHandler.sendToServer(new SlimeVacModePacket());
        }
    }
}
