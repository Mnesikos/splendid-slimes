package io.github.chakyl.splendidslimes.entity;

import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class SplendidSlime extends SlimeEntityBase  {
    public static final String SLIME = "slime";
    public static final String ID = "id";
    public static final String DATA = "data";
    public boolean aiItemFlag = false;
    private final EntityType<SlimeEntityBase> entityType;
    public static final EntityDataAccessor<String> TYPE = SynchedEntityData.defineId(SlimeEntityBase.class, EntityDataSerializers.STRING);

    public SplendidSlime(EntityType<SlimeEntityBase> entityType, Level level) {
        super(entityType, level);
        this.entityType = entityType;
    }

    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new SplendidSlime.SlimeTargetItemGoal(this));
    }

    public EntityType<SlimeEntityBase> getEntityType() {
        return entityType;
    }

    public boolean canPickUpLoot() {
        return true;
    }

    public boolean wantsToPickUp(ItemStack pStack) {
        Item pickUpItem = pStack.getItem();
        SlimeBreed slime = getSlime().get();
        if (pStack == slime.favoriteFood()) return true;
        for (ItemStack item : slime.foods()) {
            if (item.getItem() == pickUpItem) return true;
        }
        return false;
    }

    public ItemStack getSlimePlort() {
        ItemStack plort = new ItemStack(ModElements.Items.PLORT.get());
        plort.getOrCreateTagElement("plort").putString(ID, getSlimeType());
        return plort;
    }


    @Override
    protected ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, getSlimePlort());

    }

    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        if (wantsToPickUp(item)) {
            item.setCount(item.getCount() - 1);
            itemEntity.setItem(item);
            this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 0.9F);
            this.spawnAtLocation(getSlimePlort());
        }
    }

    static class SlimeTargetItemGoal extends Goal {
        private final SplendidSlime slime;
        private ItemEntity targetItem = null;

        public SlimeTargetItemGoal(SplendidSlime slime) {
            this.slime = slime;
        }

        @Override
        public boolean canUse() {
            // Find the nearest ItemEntity within a range of 10 blocks
            List<ItemEntity> nearbyItems = slime.level().getEntitiesOfClass(ItemEntity.class, slime.getBoundingBox().inflate(10));
            if (nearbyItems.isEmpty()) {
                return false;
            }
            for (ItemEntity item : nearbyItems ) {
                if (slime.wantsToPickUp(item.getItem())) targetItem = item;
            }
            return targetItem != null;
        }

        @Override
        public void tick() {
            if (targetItem != null) slime.getNavigation().moveTo(targetItem, 0.8D);
        }

        @Override
        public boolean canContinueToUse() {
            return targetItem != null && targetItem.isAlive();
        }

        @Override
        public void stop() {
            targetItem = null;
        }
    }
}
