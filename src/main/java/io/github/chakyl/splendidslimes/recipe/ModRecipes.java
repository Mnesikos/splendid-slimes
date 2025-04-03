package io.github.chakyl.splendidslimes.recipe;

import io.github.chakyl.splendidslimes.SplendidSlimes;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SplendidSlimes.MODID);

    public static final RegistryObject<RecipeSerializer<PlortPressRecipe>> PLORT_PRESSING_SERIALIZER =
            SERIALIZERS.register("plort_pressing", () -> PlortPressRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
