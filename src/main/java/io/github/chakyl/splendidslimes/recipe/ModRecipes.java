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

    public static final RegistryObject<RecipeSerializer<PlortPressingRecipe>> PLORT_PRESSING_SERIALIZER =
            SERIALIZERS.register("plort_pressing", () -> PlortPressingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<PlortRippingRecipe>> PLORT_RIPPING_SERIALIZER =
            SERIALIZERS.register("plort_ripping", () -> PlortRippingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
