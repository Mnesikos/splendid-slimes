package io.github.chakyl.splendidslimes.jade;

import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.entity.SplendidSlime;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class SlimeInfoPlugin implements IWailaPlugin {
    public static final ResourceLocation UID = new ResourceLocation(SplendidSlimes.MODID, "splendid_slime");
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(SlimeInfoComponentProvider.INSTANCE, SplendidSlime.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(SlimeInfoComponentProvider.INSTANCE, SplendidSlime.class);
    }
}