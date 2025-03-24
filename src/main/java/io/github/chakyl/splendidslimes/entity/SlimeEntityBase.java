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
    private static final EntityDataAccessor<Boolean> HAS_SPLIT = SynchedEntityData.defineId(SlimeEntityBase.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> BREED = SynchedEntityData.defineId(SlimeEntityBase.class, EntityDataSerializers.STRING);

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
        this.entityData.set(BREED, data);
    }

    public String getSlimeType() {
        return this.entityData.get(BREED);
    }

    public void setHasSplit(Boolean data) {
        this.entityData.set(HAS_SPLIT, data);
    }

    public Boolean getHasSplit() {
        return this.entityData.get(HAS_SPLIT);
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
        this.entityData.define(BREED, "");
        this.entityData.define(HAS_SPLIT, false);
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
        setSlimeType(nbt.getString("Breed"));
        setHasSplit(nbt.getBoolean("HasSplit"));
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("Breed", getSlimeType());
        nbt.putBoolean("HasSplit", getHasSplit());
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setHasSplit(false);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        int i = this.getSize();
        if (!this.level().isClientSide && i > 1 && this.isDeadOrDying()) {
            Component component = this.getCustomName();
            boolean flag = this.isNoAi();
            float f = (float)i / 4.0F;
            int j = i / 2;
            int k = 2 + this.random.nextInt(3);

            for(int l = 0; l < k; ++l) {
                float f1 = ((float)(l % 2) - 0.5F) * f;
                float f2 = ((float)(l / 2) - 0.5F) * f;
                SlimeEntityBase slime = (SlimeEntityBase)this.getType().create(this.level());
                if (slime != null) {
                    if (this.isPersistenceRequired()) {
                        slime.setPersistenceRequired();
                    }
                    slime.setSlimeType(this.getSlimeType());
                    slime.setHasSplit(true);
                    slime.setCustomName(component);
                    slime.setNoAi(flag);
                    slime.setInvulnerable(this.isInvulnerable());
                    slime.setSize(j, true);
                    slime.moveTo(this.getX() + (double)f1, this.getY() + (double)0.5F, this.getZ() + (double)f2, this.random.nextFloat() * 360.0F, 0.0F);
                    this.level().addFreshEntity(slime);
                }
            }
        }
        this.setRemoved(pReason);
        this.invalidateCaps();
        this.brain.clearMemories();
    }

}