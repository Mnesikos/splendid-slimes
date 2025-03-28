package io.github.chakyl.splendidslimes.JEI;

import io.github.chakyl.splendidslimes.data.SlimeBreed;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PlortRippingRecipe {

    final ItemStack input;
    final List<ItemStack> outputs;

    public PlortRippingRecipe(SlimeBreed breed) {
        this.input = breed.getPlortResources().copy();
        this.outputs = breed.plortResources();
    }
}
