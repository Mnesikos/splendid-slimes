package io.github.chakyl.splendidslimes.util;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SlimeData {
    public static DynamicHolder<SlimeBreed> getSlimeData(ItemStack stack, String nbtAccessor) {
        CompoundTag tag = stack.getTagElement(nbtAccessor);
        if (stack.isEmpty() || tag == null || !tag.contains("id")) {
            return SlimeBreedRegistry.INSTANCE.emptyHolder();
        }
        return SlimeBreedRegistry.INSTANCE.holder(new ResourceLocation(tag.getString("id")));
    }

}
