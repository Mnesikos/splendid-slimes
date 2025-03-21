package io.github.chakyl.splendidslimes.entity;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SlimeEntityBase extends Slime {
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(SlimeEntityBase.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> TYPE = SynchedEntityData.defineId(SlimeEntityBase.class, EntityDataSerializers.STRING);

    public SlimeEntityBase(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public void setSlimeType(String data) {
        this.entityData.set(TYPE, data);
    }

    public String getSlimeType() {
        return this.entityData.get(TYPE);
    }
    @Override
    protected @NotNull Component getTypeName() {
        DynamicHolder<SlimeBreed> slime = getSlime();
        Component slimeName;
        if (!slime.isBound()) {
            slimeName = Component.literal("BROKEN").withStyle(ChatFormatting.OBFUSCATED);
        }
        else slimeName = slime.get().name();
        return Component.translatable("entity.splendid_slimes.splendid_slime", slimeName);
    }

    public int getSlimeColor() {
        DynamicHolder<SlimeBreed> slime = getSlime();
        return slime.get().getColor();
    }

    public DynamicHolder<SlimeBreed> getSlime() {
        String type = getSlimeType();
        if (type.isEmpty()) {
            return SlimeBreedRegistry.INSTANCE.emptyHolder();
        }
        return SlimeBreedRegistry.INSTANCE.holder(new ResourceLocation(type));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TYPE, "");
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> param) {
        if (TYPE.equals(param)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(param);
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setSlimeType(nbt.getString("type"));
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("type", getSlimeType());
    }

}