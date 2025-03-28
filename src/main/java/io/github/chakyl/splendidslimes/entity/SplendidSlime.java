package io.github.chakyl.splendidslimes.entity;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.SplendidSlimes;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

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
        this.targetSelector.addGoal(1, new SplendidSlime.SlimeAttackFoodEntities(this, true, 10, null));

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
        return getSlimePlort(false);
    }

    public ItemStack getSlimePlort(Boolean secondary) {
        ItemStack plort = new ItemStack(ModElements.Items.PLORT.get());
        if (secondary) {
            plort.getOrCreateTagElement("plort").putString(ID, getSlimeSecondaryBreed());
        } else {
            plort.getOrCreateTagElement("plort").putString(ID, getSlimeBreed());
        }
        return plort;
    }


    public List<EntityType<? extends LivingEntity>> getEdibleMobs() {
        DynamicHolder<SlimeBreed> slime = getSlime();
        if (!slime.isBound()) return null;
        return slime.get().entities();
    }

    public void push(Entity pEntity) {
        super.push(pEntity);
        List<EntityType<? extends LivingEntity>> edibleMobs = getEdibleMobs();
        for (EntityType mobType : edibleMobs) {
            if (pEntity.getType() == mobType && this.isDealsDamage()) {
                this.dealDamage((LivingEntity)pEntity);
                if (((LivingEntity) pEntity).getHealth() <= 0 && ((LivingEntity) pEntity).getKillCredit() == this) {
                    handleFeed(true);
                }
            }
        }
    }

    private void handleFeed(boolean isFavorite) {
        ItemStack dropOne = getSlimePlort();
        int size = this.getSize();
        if (size >= 3) {
            if (isFavorite) dropOne.setCount(2);
            this.spawnAtLocation(dropOne);
            if (!this.getSlimeSecondaryBreed().isEmpty()) {
                ItemStack dropTwo = getSlimePlort(true);
                if (isFavorite) dropTwo.setCount(2);
                this.spawnAtLocation(dropTwo);
                if (size == 3) this.setSize(size + 1, true);
            }
        } else {
            this.setSize(size + 1, true);
        }
        this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 0.9F);
        this.eatingCooldown = SLIME_EAT_COOLDOWN;
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
                handleFeed(false);
//                if (plortDrop != null) plortDrop.setDeltaMovement(itemEntity.getDeltaMovement().add(this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.3F, this.random.nextFloat() * 0.3F));
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

    public class SlimeAttackFoodEntities<T extends LivingEntity> extends TargetGoal {
        protected LivingEntity target;
        protected TargetingConditions targetConditions;
        protected final int randomInterval;

        public SlimeAttackFoodEntities(Mob pMob, boolean pMustSee,int pRandomInterval, @Nullable Predicate<LivingEntity> pTargetPredicate) {
            super(pMob, pMustSee);
            this.randomInterval = reducedTickDelay(pRandomInterval);
            this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(pTargetPredicate);
        }

        protected void findTarget() {
            List<EntityType<? extends LivingEntity>> edibleMobs = getEdibleMobs();
            List<LivingEntity> nearbyEntities = this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(10), e -> edibleMobs.contains(e.getType()));
            if (!nearbyEntities.isEmpty()) {
                LivingEntity targetEntity = nearbyEntities.get(0);
                for (LivingEntity potentialTarget : nearbyEntities) {
                    if (this.mob.distanceToSqr(potentialTarget) < this.mob.distanceToSqr(targetEntity)) {
                        targetEntity = potentialTarget;
                    }
                }
                this.target = targetEntity;
            }
        }

        public boolean canUse() {
            if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
                return false;
            } else {
                this.findTarget();
                return this.target != null;
            }
        }
        public void start() {
            this.mob.setTarget(this.target);
            super.start();
        }

    }
}
