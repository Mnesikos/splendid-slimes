package io.github.chakyl.splendidslimes.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import org.jetbrains.annotations.Nullable;

public class PlortPressRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack input;
    private final ItemStack output;
    private final ResourceLocation id;

    public PlortPressRecipe(NonNullList<Ingredient> inputItems, ItemStack input, ItemStack output, ResourceLocation id) {
        this.inputItems = inputItems;
        this.input = input;
        this.output = output;
        this.id = id;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if(pLevel.isClientSide()) {
            return false;
        }

        return input.equals(pContainer.getItem(0), true);
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public ItemStack getInputItem(RegistryAccess pRegistryAccess) {
        return input.copy();
    }
    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<PlortPressRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "plort_pressing";
    }

    public static class Serializer implements RecipeSerializer<PlortPressRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(SplendidSlimes.MODID, "plort_pressing");

        @Override
        public PlortPressRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(1, Ingredient.EMPTY);

            for(int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }
            ItemStack inputItem = ItemAdapter.ITEM_READER.fromJson(ingredients.get(0), ItemStack.class);

            return new PlortPressRecipe(inputs, inputItem, output, pRecipeId);
        }

        @Override
        public @Nullable PlortPressRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(), Ingredient.EMPTY);

            for(int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack input = pBuffer.readItem();
            ItemStack output = pBuffer.readItem();
            return new PlortPressRecipe(inputs, inputs.get(0).getItems()[0], output, pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, PlortPressRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.inputItems.size());

            for (Ingredient ingredient :pRecipe .getIngredients()) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItemStack(pRecipe.getInputItem(null), false);
            pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
        }
    }
}
