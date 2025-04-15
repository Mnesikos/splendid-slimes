package io.github.chakyl.splendidslimes.item;

import dev.shadowsoffire.placebo.tabs.ITabFiller;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HatItem extends Item {
    public static final String SLIME = "slime";
    public static final String ID = "id";
    public static final String DATA = "data";

    public HatItem(Properties pProperties) {
        super(pProperties);
    }

    public static void setStoredHat(ItemStack stack, SlimeBreed slimeBreed) {
        setStoredHat(stack, SlimeBreedRegistry.INSTANCE.getKey(slimeBreed));
    }

    public static void setStoredHat(ItemStack stack, ResourceLocation location) {
        stack.removeTagKey(SLIME);
        stack.getOrCreateTagElement(SLIME).putString(ID, location.toString());
    }

    public static int getData(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(SLIME);
        return stack.isEmpty() || tag == null ? 0 : tag.getInt(DATA);
    }

    public static void setData(ItemStack stack, int data) {
        stack.getOrCreateTagElement(SLIME).putInt(DATA, data);
    }
}