package io.github.chakyl.splendidslimes.JEI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import io.github.chakyl.splendidslimes.registry.ModElements;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static io.github.chakyl.splendidslimes.util.SlimeData.getSlimeData;

@JeiPlugin
public class SplendidSlimesJei implements IModPlugin {

    public static final ResourceLocation UID = new ResourceLocation(SplendidSlimes.MODID, "plugin");

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerSubtypeInterpreter(ModElements.Items.PLORT.get(), new ModelSubtypes());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new PlortRippitCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
               List<PlortRippingRecipe> plortRippingRecipes = new ArrayList<>();
        for (SlimeBreed breed : SlimeBreedRegistry.INSTANCE.getValues()) {
            plortRippingRecipes.add(new PlortRippingRecipe(breed));
        }
        reg.addRecipes(PlortRippitCategory.TYPE, plortRippingRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(ModElements.Blocks.PLORT_RIPPIT.get()), PlortRippitCategory.TYPE);
    }

    private static class ModelSubtypes implements IIngredientSubtypeInterpreter<ItemStack> {

        @Override
        public String apply(ItemStack stack, UidContext context) {
            DynamicHolder<SlimeBreed> dm = getSlimeData(stack, "plort");
            if (!dm.isBound()) return "NULL";
            return dm.getId().toString();
        }

    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

}