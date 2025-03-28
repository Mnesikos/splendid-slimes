package io.github.chakyl.splendidslimes.data;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all of the information representing a Slime.
 *
 * @param breed            The breed of the slime
 * @param name             The display name of slime
 * @param foods            List of items or item tags a Slime will eat
 * @param favoriteFood     The itemstack for a slime's favorite food that doubles plort output
 * @param entities            List of Entities a Slime will attack and eat
 * @param favoriteEntity     The Entity for a slime's favorite food that doubles plort output
 * @param plortResources   List of items produced from a plort.
 */
public record SlimeBreed(String breed, MutableComponent name, List<ItemStack> foods, ItemStack favoriteFood, List<EntityType<? extends LivingEntity>> entities, EntityType<? extends LivingEntity> favoriteEntity, List<ItemStack> plortResources) implements CodecProvider<SlimeBreed> {

    public static final Codec<SlimeBreed> CODEC = new SlimeBreedCodec();

    public SlimeBreed(SlimeBreed other) {
        this(other.breed, other.name, other.foods, other.favoriteFood, other.entities, other.favoriteEntity, other.plortResources);
    }

    public int getColor() {
        return this.name.getStyle().getColor().getValue();
    }

    public SlimeBreed validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.breed, "Invalid slime name!");
        Preconditions.checkNotNull(this.name, "Invalid slime name!");
        Preconditions.checkNotNull(this.name.getStyle().getColor(), "Invalid entity name color!");
//        Preconditions.checkNotNull(this.foods, "Missing foods!");
//        this.foods.forEach(t -> Preconditions.checkArgument(t != null && !t.isEmpty(), "Invalid plort foods!"));
//        Preconditions.checkNotNull(this.entities, "Missing Entities !");
//        this.entities.forEach(t -> Preconditions.checkArgument(t != null && !t.isEmpty(), "Invalid plort foods!"));
//        Preconditions.checkNotNull(this.favoriteEntity, "Invalid favorite food!");
        Preconditions.checkNotNull(this.plortResources, "Missing plort resources!");
        this.plortResources.forEach(t -> Preconditions.checkArgument(t != null && !t.isEmpty(), "Invalid plort resources!"));
        return this;
    }

    @Override
    public Codec<? extends SlimeBreed> getCodec() {
        return CODEC;
    }

    public static class SlimeBreedCodec implements Codec<SlimeBreed> {

        @Override
        public <T> DataResult<T> encode(SlimeBreed input, DynamicOps<T> ops, T prefix) {
            JsonObject obj = new JsonObject();
            ResourceLocation key = new ResourceLocation(SplendidSlimes.MODID, input.breed);
            obj.addProperty("breed", input.breed);
            obj.addProperty("name", ((TranslatableContents) input.name.getContents()).getKey());
            obj.addProperty("color", input.name.getStyle().getColor().serialize());
            JsonArray foods = ItemAdapter.ITEM_READER.toJsonTree(input.foods).getAsJsonArray();
            for (JsonElement e : foods) {
                JsonObject drop = e.getAsJsonObject();
                ResourceLocation itemName = new ResourceLocation(drop.get("item").getAsString());
                if (!"minecraft".equals(itemName.getNamespace()) && !key.getNamespace().equals(itemName.getNamespace())) {
                    drop.addProperty("optional", true);
                }
            }
            obj.add("foods", foods);
            obj.add("favorite_food", ItemAdapter.ITEM_READER.toJsonTree(input.favoriteFood));
            obj.addProperty("favorite_entity", EntityType.getKey(input.favoriteEntity).toString());
            obj.add("entities", ItemAdapter.ITEM_READER.toJsonTree(input.entities.stream().map(EntityType::getKey).toList()));
            JsonArray plortResources = ItemAdapter.ITEM_READER.toJsonTree(input.plortResources).getAsJsonArray();
            for (JsonElement e : plortResources) {
                JsonObject drop = e.getAsJsonObject();
                ResourceLocation itemName = new ResourceLocation(drop.get("item").getAsString());
                if (!"minecraft".equals(itemName.getNamespace()) && !key.getNamespace().equals(itemName.getNamespace())) {
                    drop.addProperty("optional", true);
                }
            }
            obj.add("plort_resources", plortResources);
            return DataResult.success(JsonOps.INSTANCE.convertTo(ops, obj));
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T> DataResult<Pair<SlimeBreed, T>> decode(DynamicOps<T> ops, T input) {
            JsonObject obj = ops.convertTo(JsonOps.INSTANCE, input).getAsJsonObject();

            String breed = GsonHelper.getAsString(obj, "breed");
            MutableComponent name = Component.translatable(GsonHelper.getAsString(obj, "name"));
            if (obj.has("color")) {
                String colorStr = GsonHelper.getAsString(obj, "color");
                var color = TextColor.parseColor(colorStr);
                name = name.withStyle(Style.EMPTY.withColor(color));
            }
            else name.withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
            List<ItemStack> foods = ItemAdapter.ITEM_READER.fromJson(GsonHelper.getAsJsonArray(obj, "foods"), new TypeToken<List<ItemStack>>(){}.getType());
            ItemStack favoriteFood = ItemAdapter.ITEM_READER.fromJson(GsonHelper.getAsJsonObject(obj, "favorite_food"), ItemStack.class);
            List<EntityType<? extends LivingEntity>> entities = new ArrayList<>();
            if (obj.has("entities")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "entities")) {
                    EntityType<? extends LivingEntity> st = (EntityType) ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(json.getAsString()));
                    if (st != EntityType.PIG || "minecraft:pig".equals(json.getAsString())) entities.add(st);
                    // Intentionally ignore invalid entries here, so that modded entities can be added as subtypes without hard deps.
                }
            }
            String favoriteEntityStr = GsonHelper.getAsString(obj, "favorite_entity");
            EntityType<? extends LivingEntity> favoriteEntity = (EntityType) ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(favoriteEntityStr));
            if (favoriteEntity == EntityType.PIG && !"minecraft:pig".equals(favoriteEntityStr)) throw new JsonParseException("Slime has invalid favorite entity type " + favoriteEntityStr);
            List<ItemStack> plortResources = ItemAdapter.ITEM_READER.fromJson(GsonHelper.getAsJsonArray(obj, "plort_resources"), new TypeToken<List<ItemStack>>(){}.getType());
            plortResources.removeIf(ItemStack::isEmpty);
            return DataResult.success(Pair.of(new SlimeBreed(breed, name, foods, favoriteFood, entities, favoriteEntity, plortResources), input));
        }

    }

}