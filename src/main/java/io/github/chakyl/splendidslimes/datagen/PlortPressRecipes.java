package io.github.chakyl.splendidslimes.datagen;

import io.github.chakyl.splendidslimes.SplendidSlimes;
import net.minecraft.client.Minecraft;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class PlortPressRecipes {
    public static void register(Consumer<FinishedRecipe> consumer) {
        List<String> breeds = Arrays.asList("all_seeing", "bitwise", "blasting", "blazing", "bony", "ender", "gold", "luminous", "minty", "orby", "phantom", "prisma", "puddle", "rotting", "shulking", "slimy", "sweet", "webby", "weeping");
        for (String breed : breeds) {
            SplendidSlimes.LOGGER.info("generating pressing recipe for " + breed);
            PlortPressRecipeBuilder.pressRecipe(breed).build(consumer);;
        }
    }
}