package io.github.chakyl.splendidslimes.jade;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.SlimyConfig;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.entity.SplendidSlime;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import static io.github.chakyl.splendidslimes.util.SlimeData.getSlimeData;

public enum SlimeInfoComponentProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            EntityAccessor entityAccessor,
            IPluginConfig config
    ) {
        boolean isLargo = entityAccessor.getServerData().contains("SecondaryBreed") && !entityAccessor.getServerData().getString("SecondaryBreed").isEmpty();
        if (entityAccessor.getServerData().contains("Breed")) {
            DynamicHolder<SlimeBreed> slime = getSlimeData(entityAccessor.getServerData().getString("Breed"));
            if (slime.isBound()) {
                if (!isLargo) {
                    tooltip.add(Component.translatable("entity.splendid_slimes.diet", slime.get().diet()));
                } else {
                    tooltip.add(Component.translatable("entity.splendid_slimes.largo_diet", slime.get().diet(), getSlimeData(entityAccessor.getServerData().getString("SecondaryBreed")).get().diet()));
                }
            }
            if (entityAccessor.getServerData().contains("EatingCooldown")) {
                int eatingCooldown = entityAccessor.getServerData().getInt("EatingCooldown");
                int maxEatingCooldown = SlimyConfig.slimeStarvingTime;
                int hunger = (int) Math.round(((double) eatingCooldown / maxEatingCooldown) * 10.0);
                IElementHelper elements = IElementHelper.get();
                ItemStack plort = ModElements.Items.PLORT.get().getDefaultInstance();
                plort.getOrCreateTagElement("plort").putString("id", entityAccessor.getServerData().getString("Breed"));
                tooltip.add(Component.translatable("entity.splendid_slimes.hunger"));
                for (int i = 0; i < 10; i++) {
                    if (i >= hunger) plort = ModElements.Items.PLORT.get().getDefaultInstance();
                    tooltip.append(elements.item(plort, 0.5f).size(new Vec2(6, 10)).translate(new Vec2(i, -1)));
                }
                tooltip.append(elements.spacer(14,2));
            }

            if (entityAccessor.getServerData().contains("Happiness")) {
                int happiness = entityAccessor.getServerData().getInt("Happiness");
                Component happinessComponent;
                if (happiness >= SplendidSlime.HAPPY_THRESHOLD) {
                    happinessComponent = Component.translatable("entity.splendid_slimes.happy").withStyle(ChatFormatting.GREEN);
                } else if (happiness <= SplendidSlime.FURIOUS_THRESHOLD) {
                    happinessComponent = Component.translatable("entity.splendid_slimes.furious").withStyle(ChatFormatting.RED);
                } else if (happiness <= SplendidSlime.UNHAPPY_THRESHOLD) {
                    happinessComponent = Component.translatable("entity.splendid_slimes.sad").withStyle(ChatFormatting.GOLD);
                } else {
                    happinessComponent = Component.translatable("entity.splendid_slimes.neutral");
                }
                tooltip.add(happinessComponent);
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        SplendidSlime slime = (SplendidSlime) accessor.getEntity();
        data.putString("Breed", slime.getEntityData().get(SplendidSlime.BREED));
        data.putString("SecondaryBreed", slime.getEntityData().get(SplendidSlime.SECONDARY_BREED));
        data.putInt("Happiness", slime.getEntityData().get(SplendidSlime.HAPPINESS));
        data.putInt("EatingCooldown", slime.getEntityData().get(SplendidSlime.EATING_COOLDOWN));
    }

    @Override
    public ResourceLocation getUid() {
        return SlimeInfoPlugin.UID;
    }

}