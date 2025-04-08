package io.github.chakyl.splendidslimes.data;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.item.PlortItem;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all of the information representing a Slime.
 *
 * @param breed               The breed of the slime
 * @param name                The display name of slime
 * @param hat                 Item to use as a hat
 * @param hatScale            Scale applied to the hat when it is being rendered.
 * @param hatXOffset          X offset applied to the hat when it is being rendered.
 * @param hatYOffset          Y offset applied to the hat when it is being rendered.
 * @param hatZOffset          Z offset applied to the hat when it is being rendered.
 * @param diet                Diet of the slime, for players. Be as vague or mysterious as needed
 * @param foods               List of items or item tags a Slime will eat
 * @param favoriteFood        The itemstack for a slime's favorite food that doubles plort output
 * @param entities            List of Entities a Slime will attack and eat
 * @param favoriteEntity      The Entity for a slime's favorite food that doubles plort output
 * @param positiveEmitEffects List of effects that will be emitted in an AoE around the slime when happy
 * @param negativeEmitEffects List of effects that will be emitted in an AoE around the slime when unhappy
 */
public record SlimeBreed(String breed, MutableComponent name, ItemStack hat, float hatScale, float hatXOffset, float hatYOffset, float hatZOffset, MutableComponent diet, List<Object> foods,
                         ItemStack favoriteFood, List<EntityType<? extends LivingEntity>> entities,
                         EntityType<? extends LivingEntity> favoriteEntity,
                         List<MobEffectInstance> positiveEmitEffects,
                         List<MobEffectInstance> negativeEmitEffects) implements CodecProvider<SlimeBreed> {

    public static final Codec<SlimeBreed> CODEC = new SlimeBreedCodec();

    public SlimeBreed(SlimeBreed other) {
        this(other.breed, other.name, other.hat, other.hatScale, other.hatXOffset, other.hatYOffset, other.hatZOffset, other.diet, other.foods, other.favoriteFood, other.entities, other.favoriteEntity, other.positiveEmitEffects, other.positiveEmitEffects);
    }

    public int getColor() {
        return this.name.getStyle().getColor().getValue();
    }

    public ItemStack getPlortResources() {
        ItemStack stk = new ItemStack(ModElements.Items.PLORT.get());
        PlortItem.setStoredPlort(stk, this);
        return stk;
    }

    public SlimeBreed validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.breed, "Invalid slime breed id!");
        Preconditions.checkNotNull(this.name, "Invalid slime name!");
        Preconditions.checkNotNull(this.hat, "Invalid slime hat!");
        Preconditions.checkNotNull(this.diet, "Invalid slime diet!");
        Preconditions.checkNotNull(this.name.getStyle().getColor(), "Invalid entity name color!");
        return this;
    }

    @Override
    public Codec<? extends SlimeBreed> getCodec() {
        return CODEC;
    }

    private static JsonObject getEffectJson(Object effect) {
        JsonObject jsonEffect = new JsonObject();
        if (effect.getClass() == MobEffectInstance.class) {
            jsonEffect.addProperty("effect", BuiltInRegistries.MOB_EFFECT.getKey(((MobEffectInstance) effect).getEffect()).toString());
            jsonEffect.addProperty("duration", ((MobEffectInstance) effect).getDuration());
            jsonEffect.addProperty("amplifier", ((MobEffectInstance) effect).getAmplifier());
        }
        return jsonEffect;
    }

    private static MobEffectInstance getEffectFromJson(JsonElement jsonElement) {
        MobEffect postiveEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("minecraft:slowness"));
        int duration = 40;
        int amplifier = 0;
        if (jsonElement.getAsJsonObject().has("effect"))
            postiveEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(jsonElement.getAsJsonObject().get("effect").getAsString()));
        if (jsonElement.getAsJsonObject().has("duration"))
            duration = jsonElement.getAsJsonObject().get("duration").getAsInt();
        if (jsonElement.getAsJsonObject().has("amplifier"))
            amplifier = jsonElement.getAsJsonObject().get("amplifier").getAsInt();
        return new MobEffectInstance(postiveEffect, duration, amplifier, false, false);
    }

    public static class SlimeBreedCodec implements Codec<SlimeBreed> {

        @Override
        public <T> DataResult<T> encode(SlimeBreed input, DynamicOps<T> ops, T prefix) {
            JsonObject obj = new JsonObject();
            ResourceLocation key = new ResourceLocation(SplendidSlimes.MODID, input.breed);
            SplendidSlimes.LOGGER.info(input);
            obj.addProperty("breed", input.breed);
            obj.addProperty("name", ((TranslatableContents) input.name.getContents()).getKey());
            obj.add("hat", ItemAdapter.ITEM_READER.toJsonTree(input.hat));
            obj.addProperty("hat_scale", input.hatScale);
            obj.addProperty("hat_x_offset", input.hatXOffset);
            obj.addProperty("hat_y_offset", input.hatYOffset);
            obj.addProperty("hat_z_offset", input.hatZOffset);
            obj.addProperty("diet", ((TranslatableContents) input.diet.getContents()).getKey());
            obj.addProperty("color", input.name.getStyle().getColor().serialize());
            JsonArray foods = new JsonArray();
            obj.add("foods", foods);
            for (Object food : input.foods) {
                if (food.getClass() == ItemStack.class) {
                    JsonElement newStack = ItemAdapter.ITEM_READER.toJsonTree(food);
                    JsonObject foodJson = newStack.getAsJsonObject();
                    ResourceLocation itemName = new ResourceLocation(foodJson.get("item").getAsString());
                    if (!"minecraft".equals(itemName.getNamespace()) && !key.getNamespace().equals(itemName.getNamespace())) {
                        foodJson.addProperty("optional", true);
                    }
                    foods.add(foodJson);
                }
                if (food.getClass() == TagKey.class) {
                    JsonObject tagJson = new JsonObject();
                    tagJson.addProperty("tag", ((TagKey<?>) food).location().toString());
                    foods.add(tagJson);
                }
            }
            obj.add("favorite_food", ItemAdapter.ITEM_READER.toJsonTree(input.favoriteFood));
            obj.addProperty("favorite_entity", EntityType.getKey(input.favoriteEntity).toString());
            obj.add("entities", ItemAdapter.ITEM_READER.toJsonTree(input.entities.stream().map(EntityType::getKey).toList()));
            JsonArray positiveEmitEffects = new JsonArray();
            obj.add("positive_emit_effects", positiveEmitEffects);
            for (Object effect : input.positiveEmitEffects) {
                positiveEmitEffects.add(getEffectJson(effect));
            }
            JsonArray negativeEmitEffects = new JsonArray();
            obj.add("negative_emit_effects", negativeEmitEffects);
            for (Object effect : input.negativeEmitEffects) {
                negativeEmitEffects.add(getEffectJson(effect));
            }
            return DataResult.success(JsonOps.INSTANCE.convertTo(ops, obj));
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> DataResult<Pair<SlimeBreed, T>> decode(DynamicOps<T> ops, T input) {
            JsonObject obj = ops.convertTo(JsonOps.INSTANCE, input).getAsJsonObject();

            SplendidSlimes.LOGGER.info("YEET");
            SplendidSlimes.LOGGER.info(obj.toString());
            String breed = GsonHelper.getAsString(obj, "breed");
            MutableComponent name = Component.translatable(GsonHelper.getAsString(obj, "name"));
            ItemStack hat = ItemAdapter.ITEM_READER.fromJson(obj.get("hat"), ItemStack.class);
            float hatScale = 1.25F;
            if (obj.has("hat_scale")) {
                hatScale = GsonHelper.getAsFloat(obj, "hat_scale");
            }
            float hatXOffset = 0;
            if (obj.has("hat_x_offset")) {
                hatXOffset = GsonHelper.getAsFloat(obj, "hat_x_offset");
            }
            float hatYOffset = -0.75F;
            if (obj.has("hat_y_offset")) {
                hatYOffset = GsonHelper.getAsFloat(obj, "hat_y_offset");
            }
            float hatZOffset = 0.02F;
            if (obj.has("hat_z_offset")) {
                hatZOffset = GsonHelper.getAsFloat(obj, "hat_z_offset");
            }
            
            MutableComponent diet = Component.translatable(GsonHelper.getAsString(obj, "diet"));
            if (obj.has("color")) {
                String colorStr = GsonHelper.getAsString(obj, "color");
                var color = TextColor.parseColor(colorStr);
                name = name.withStyle(Style.EMPTY.withColor(color));
            } else name.withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
            List<Object> foods = new ArrayList<>();
            if (obj.has("foods")) {
                JsonArray parsedFood = GsonHelper.getAsJsonArray(obj, "foods");
                for (JsonElement e : parsedFood) {
                    if (e.getAsJsonObject().has("item"))
                        foods.add(ItemAdapter.ITEM_READER.fromJson(e.getAsJsonObject(), ItemStack.class));
                    else if (e.getAsJsonObject().has("tag"))
                        foods.add(TagKey.create(Registries.ITEM, new ResourceLocation(e.getAsJsonObject().get("tag").getAsString())));
                }
            }
            ItemStack favoriteFood = new ItemStack(Items.AIR);
            if (obj.has("favorite_food")) {
                favoriteFood = ItemAdapter.ITEM_READER.fromJson(obj.get("favorite_food"), ItemStack.class);
            }
            List<EntityType<? extends LivingEntity>> entities = new ArrayList<>();
            if (obj.has("entities")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "entities")) {
                    EntityType<? extends LivingEntity> st = (EntityType) ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(json.getAsString()));
                    if (st != EntityType.PIG || "minecraft:pig".equals(json.getAsString())) entities.add(st);
                    // Intentionally ignore invalid entries here, so that modded entities can be added as subtypes without hard deps.
                }
            }
            EntityType<? extends LivingEntity> favoriteEntity = null;
            if (obj.has("favorite_entity")) {
                String favoriteEntityStr = GsonHelper.getAsString(obj, "favorite_entity");
                favoriteEntity = (EntityType) ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(favoriteEntityStr));
                if (favoriteEntity == EntityType.PIG && !"minecraft:pig".equals(favoriteEntityStr))
                    throw new JsonParseException("Slime has invalid favorite entity type " + favoriteEntityStr);
            }
            List<MobEffectInstance> positiveEmitEffects = new ArrayList<>();
            if (obj.has("positive_emit_effects")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "positive_emit_effects")) {
                    positiveEmitEffects.add(getEffectFromJson(json));
                }
            }
            List<MobEffectInstance> negativeEmitEffects = new ArrayList<>();
            if (obj.has("negative_emit_effects")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "negative_emit_effects")) {
                    negativeEmitEffects.add(getEffectFromJson(json));
                }
            }
            return DataResult.success(Pair.of(new SlimeBreed(breed, name, hat, hatScale, hatXOffset, hatYOffset, hatZOffset, diet, foods, favoriteFood, entities, favoriteEntity, positiveEmitEffects, negativeEmitEffects), input));
        }

    }

}