package io.github.chakyl.splendidslimes.JEI;

import io.github.chakyl.splendidslimes.data.SlimeBreed;
import net.minecraft.world.item.ItemStack;

public class TraitRecipe {
    final ItemStack slime;
    final String trait;

    public TraitRecipe(SlimeBreed breed, String trait) {
        this.slime = breed.getSlimeItem().copy();
        this.trait = trait;
    }
}