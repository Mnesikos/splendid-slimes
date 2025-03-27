package io.github.chakyl.splendidslimes.entity;

import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class SplendidSlime extends SlimeEntityBase  {
    public static final String SLIME = "slime";
    public static final String ID = "id";
    public static final String DATA = "data";
    public static final int SLIME_EAT_COOLDOWN = 20;
    private int eatingCooldown = 0;
    private final EntityType<SlimeEntityBase> entityType;

    public SplendidSlime(EntityType<SlimeEntityBase> entityType, Level level) {
        super(entityType, level);
        this.entityType = entityType;
    }

    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new SplendidSlime.SlimeTargetItemGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Chicken.class, true));
    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if(eatingCooldown > 0){
                eatingCooldown--;
            }
        }
    }

    public EntityType<SlimeEntityBase> getEntityType() {
        return entityType;
    }

    public boolean canPickUpLoot() {
        return true;
    }

    public boolean wantsToPickUp(ItemStack pStack) {
        if (this.eatingCooldown > 0) return false;
        if (!getSlime().isBound()) return false;
        Item pickUpItem = pStack.getItem();
        SlimeBreed slime = getSlime().get();
        if (pickUpItem == ModElements.Items.PLORT.get() && pStack.hasTag()) {
            CompoundTag plortTag = pStack.getTagElement("plort");
            if (plortTag != null && plortTag.contains("id")) {
                String id = plortTag.get("id").toString();
                return !id.contains(this.getSlimeBreed()) && !(!this.getSlimeSecondaryBreed().isEmpty() && id.contains(this.getSlimeSecondaryBreed()));
            }
        }
        if (pStack == slime.favoriteFood()) return true;
        for (ItemStack item : slime.foods()) {
            if (item.getItem() == pickUpItem) return true;
        }
        return false;
    }

    public ItemStack getSlimePlort() {
        ItemStack plort = new ItemStack(ModElements.Items.PLORT.get());
        plort.getOrCreateTagElement("plort").putString(ID, getSlimeBreed());
        return plort;
    }


    @Override
    protected ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, getSlimePlort());

    }

    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        boolean atePlort = false;
        if (wantsToPickUp(item)) {
            if (item.getItem() == ModElements.Items.PLORT.get() && item.hasTag()) {
                CompoundTag plortTag = item.getTagElement("plort");
                if (plortTag != null && plortTag.contains("id")) {
                    atePlort = true;
                    if (this.getSlimeSecondaryBreed().isEmpty()) {
                        if (this.getSize() < 4) this.setSize(this.getSize() + 1, false);
                        this.playSound(SoundEvents.AMETHYST_BLOCK_STEP, 1.0F, 0.9F);
                        this.setSlimeSecondaryBreed(plortTag.get("id").toString().replace("\"", ""));
                    }
                }
            }
            item.setCount(item.getCount() - 1);
            itemEntity.setItem(item);
            if (!atePlort) {
                ItemEntity plortDrop = this.spawnAtLocation(getSlimePlort());
                this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 0.9F);
                this.eatingCooldown = SLIME_EAT_COOLDOWN;
                if (plortDrop != null)
                    plortDrop.setDeltaMovement(itemEntity.getDeltaMovement().add(this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.3F));
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        int size = this.getSize();
        if (!this.level().isClientSide && size > 1 && this.isDeadOrDying()) {
            Component component = this.getCustomName();
            boolean flag = this.isNoAi();
            SlimeEntityBase slime = (SlimeEntityBase)this.getType().create(this.level());
            if (slime != null) {
                if (this.isPersistenceRequired()) {
                    slime.setPersistenceRequired();
                }
                String secondaryBreed = this.getSlimeSecondaryBreed();
                slime.setSlimeBreed(this.getSlimeBreed());
                if (!secondaryBreed.isEmpty()) slime.setSlimeSecondaryBreed(secondaryBreed);
                slime.setHasSplit(true);
                slime.setCustomName(component);
                slime.setNoAi(flag);
                slime.setInvulnerable(this.isInvulnerable());
                slime.setSize(size - 1, true);
                slime.moveTo(this.getX() , this.getY() + (double)0.5F, this.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
                this.level().addFreshEntity(slime);
            }
        }
        this.setRemoved(pReason);
        this.invalidateCaps();
        this.brain.clearMemories();
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
            if (slime.eatingCooldown > 0) return false;
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
