package io.github.chakyl.splendidslimes.util;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectUtils {
    public static DynamicHolder<SlimeBreed> getSlimeData(String breed) {
        if (breed.isEmpty()) {
            return SlimeBreedRegistry.INSTANCE.emptyHolder();
        }
        return SlimeBreedRegistry.INSTANCE.holder(new ResourceLocation(breed));
    }

}
