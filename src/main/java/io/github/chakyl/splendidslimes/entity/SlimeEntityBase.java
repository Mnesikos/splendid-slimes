package io.github.chakyl.splendidslimes.entity;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SlimeEntityBase extends Slime {
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(SlimeEntityBase.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> BREED = SynchedEntityData.defineId(SlimeEntityBase.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> SECONDARY_BREED = SynchedEntityData.defineId(SlimeEntityBase.class, EntityDataSerializers.STRING);

    public SlimeEntityBase(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }


    public void setSlimeBreed(String data) {
        this.entityData.set(BREED, data);
    }

    public String getSlimeBreed() {
        if (this instanceof Tarr) return SplendidSlimes.MODID + ":tarr";
        return this.entityData.get(BREED);
    }

    public void setSlimeSecondaryBreed(String data) {
        this.entityData.set(SECONDARY_BREED, data);
    }

    public String getSlimeSecondaryBreed() {
        return this.entityData.get(SECONDARY_BREED);
    }

    @Override
    protected @NotNull Component getTypeName() {
        DynamicHolder<SlimeBreed> slime = getSlime();
        DynamicHolder<SlimeBreed> secondarySlime = getSecondarySlime();
        Component slimeName;
        if (!slime.isBound()) {
            slimeName = Component.literal("BROKEN").withStyle(ChatFormatting.OBFUSCATED);
        } else slimeName = slime.get().name();
        if (secondarySlime.isBound()) {
            return Component.translatable("entity.splendid_slimes.largo_splendid_slime", slimeName, secondarySlime.get().name());
        }
        return Component.translatable("entity.splendid_slimes.splendid_slime", slimeName);
    }

    public int getSlimeColor() {
        DynamicHolder<SlimeBreed> slime = getSlime();
        if (!slime.isBound()) return 0xFFFFFF;
        return slime.get().getColor();
    }

    public int getSecondarySlimeColor() {
        DynamicHolder<SlimeBreed> slime = getSecondarySlime();
        if (!slime.isBound()) return -1;
        return slime.get().getColor();
    }

    public DynamicHolder<SlimeBreed> getSlime() {
        String type = getSlimeBreed();
        if (type.isEmpty()) {
            return SlimeBreedRegistry.INSTANCE.emptyHolder();
        }
        return SlimeBreedRegistry.INSTANCE.holder(new ResourceLocation(type));
    }

    public DynamicHolder<SlimeBreed> getSecondarySlime() {
        String type = getSlimeSecondaryBreed();
        if (type.isEmpty()) {
            return SlimeBreedRegistry.INSTANCE.emptyHolder();
        }
        return SlimeBreedRegistry.INSTANCE.holder(new ResourceLocation(type));
    }

    public DynamicHolder<SlimeBreed> getHatSlime() {
        DynamicHolder<SlimeBreed> slime = getSecondarySlime();
        if (!slime.isBound()) {
            slime = getSlime();
        }
        return slime;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BREED, "");
        this.entityData.define(SECONDARY_BREED, "");
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> param) {
        if (BREED.equals(param)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(param);
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setSlimeBreed(nbt.getString("Breed"));
        setSlimeSecondaryBreed(nbt.getString("SecondaryBreed"));
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("Breed", getSlimeBreed());
        nbt.putString("SecondaryBreed", getSlimeSecondaryBreed());
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setPersistenceRequired();
        SpawnGroupData spawn = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        this.setSize(0, true);
        return spawn;
    }

    @Override
    protected boolean isDealsDamage() {
        return this.isEffectiveAi();
    }

}