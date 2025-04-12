package io.github.chakyl.splendidslimes.entity;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import static io.github.chakyl.splendidslimes.util.EffectUtils.copyEffect;

public class SplendidSlime extends SlimeEntityBase {
    private static final EntityDataAccessor<Integer> HAPPINESS = SynchedEntityData.defineId(SplendidSlime.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EATING_COOLDOWN = SynchedEntityData.defineId(SplendidSlime.class, EntityDataSerializers.INT);
    public static final String SLIME = "slime";
    public static final String ID = "id";
    public static final String DATA = "data";
    public static final int SLIME_EAT_COOLDOWN = 6000;
    public static final int SLIME_HUNGRY_THRESHOLD = 1200;
    public static final int MAX_HAPPINESS = 1000;
    public ItemEntity itemTarget = null;
    boolean tarred;
    private final EntityType<SlimeEntityBase> entityType;

    public SplendidSlime(EntityType<SlimeEntityBase> entityType, Level level) {
        super(entityType, level);
        this.entityType = entityType;
        this.tarred = false;
        this.moveControl = new SplendidSlime.SlimeMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SplendidSlime.SlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new SplendidSlime.SlimeTargetItemGoal(this, 60));
        this.goalSelector.addGoal(2, new SplendidSlime.SlimeAttackFoodEntities(this, true, 60, null));
        this.goalSelector.addGoal(2, new SplendidSlime.SlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new SplendidSlime.SlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new SplendidSlime.SlimeKeepOnJumpingGoal(this));

    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            int eatCooldown = getEatingCooldown();
            if (eatCooldown > 0) {
                setEatingCooldown(eatCooldown - 1);
            }
            if (this.tickCount % 800 == 0) {
                DynamicHolder<SlimeBreed> slime = getSlime();
                if (slime.isBound()) {
                    Double chance = 1 - (((getHappiness() + 1.0) / MAX_HAPPINESS));
                    if (this.random.nextFloat() <= chance) {
                        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10));
                        for (LivingEntity entity : nearbyEntities) {
                            applyNegativeEffects(entity, slime);
                        }
                        List<String> commands = slime.get().positiveCommands();
                        if (!commands.isEmpty()) runCommands(commands);
                    } else {
                        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10));
                        for (LivingEntity entity : nearbyEntities) {
                            applyPositiveEffects(entity, slime);
                        }
                        List<String> commands = slime.get().negativeCommands();
                        if (!commands.isEmpty()) runCommands(commands);
                    }
                }
            }
            if (this.tickCount % 600 == 0) {
                setHappiness(getHappiness() - 1);
            }
        }
    }

    public EntityType<SlimeEntityBase> getEntityType() {
        return entityType;
    }

    public boolean canPickUpLoot() {
        return true;
    }

    private boolean notHungry() {
        return getEatingCooldown() > SLIME_HUNGRY_THRESHOLD;
    }

    private boolean checkFoods(ItemStack pStack, List<Object> foods) {
        Item pickUpItem = pStack.getItem();
        for (Object food : foods) {
            if (food.getClass() == ItemStack.class && ((ItemStack) food).getItem() == pickUpItem) return true;
            if (food.getClass() == TagKey.class && pStack.is((TagKey<Item>) food)) return true;
        }
        return false;
    }

    public boolean wantsToPickUp(ItemStack pStack) {
        Item pickUpItem = pStack.getItem();
        if (notHungry() && pickUpItem != ModElements.Items.PLORT.get()) return false;

        if (!getSlime().isBound()) return false;
        SlimeBreed slime = getSlime().get();
        if (pickUpItem == ModElements.Items.PLORT.get() && pStack.hasTag()) {
            CompoundTag plortTag = pStack.getTagElement("plort");
            if (plortTag != null && plortTag.contains("id")) {
                String id = plortTag.get("id").toString();
                return !id.contains(this.getSlimeBreed()) && !(!this.getSlimeSecondaryBreed().isEmpty() && id.contains(this.getSlimeSecondaryBreed()));
            }
        }
        if (pStack == slime.favoriteFood()) return true;
        if (checkFoods(pStack, slime.foods())) return true;
        if (getSecondarySlime().isBound()) {
            SlimeBreed secondarySlime = getSecondarySlime().get();
            if (pStack == secondarySlime.favoriteFood()) return true;
            return checkFoods(pStack, secondarySlime.foods());
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

    public Boolean hasSameBreed(LivingEntity livingEntity) {
        if (livingEntity.getType() == this.getType()) {
            SynchedEntityData entityData = livingEntity.getEntityData();
            if (entityData.get(BREED).equals(this.getSlimeBreed())) return true;
            return entityData.get(SECONDARY_BREED).equals(this.getSlimeBreed());
        }
        return false;
    }

    public void applyNegativeEffects(LivingEntity entity, DynamicHolder<SlimeBreed> slime) {
        if (!(hasSameBreed(entity))) {
            List<MobEffectInstance> effects = slime.get().negativeEmitEffects();
            if (!effects.isEmpty()) {
                applyEffects(entity, effects);
            }
        }
    }

    public void applyPositiveEffects(LivingEntity entity, DynamicHolder<SlimeBreed> slime) {
        List<MobEffectInstance> effects = slime.get().positiveEmitEffects();
        if (!effects.isEmpty()) {
            applyEffects(entity, effects);
        }
    }

    public void applyEffects(LivingEntity entity, List<MobEffectInstance> effects) {
        for (MobEffectInstance effect : effects) {
            entity.addEffect(copyEffect(effect));
        }
    }

    private static CommandSourceStack createCommandSourceStack(@Nullable Player pPlayer, Level pLevel, BlockPos pPos, Component displayName) {
        String s = pPlayer == null ? "Splendid Slime" : pPlayer.getName().getString();
        Component component = (Component) (pPlayer == null ? displayName : pPlayer.getDisplayName());
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(pPos), Vec2.ZERO, (ServerLevel) pLevel, 2, s, component, pLevel.getServer(), pPlayer);
    }

    public void runCommands(List<String> commands) {
        CommandSourceStack source = createCommandSourceStack((Player) null, this.level(), this.getOnPos(), this.getDisplayName());
        source.withEntity(this);
        source.withSuppressedOutput();
        for (String command : commands) {
            SplendidSlimes.LOGGER.info(this.getServer().getCommands().performPrefixedCommand(source, command.replace("\"", "")));
        }
    }

    public boolean isFavoriteFood(Item item) {
        boolean favorite;
        DynamicHolder<SlimeBreed> slime = getSlime();
        if (!slime.isBound()) return false;
        favorite = item == slime.get().favoriteFood().getItem();
        DynamicHolder<SlimeBreed> secondarySlime = getSecondarySlime();
        if (!favorite && secondarySlime.isBound()) favorite = item == secondarySlime.get().favoriteFood().getItem();
        return favorite;
    }

    public boolean getFavoriteEntity(EntityType<? extends LivingEntity> entityType) {
        boolean favorite;
        DynamicHolder<SlimeBreed> slime = getSlime();
        if (!slime.isBound()) return false;
        favorite = entityType == slime.get().favoriteEntity();
        DynamicHolder<SlimeBreed> secondarySlime = getSecondarySlime();
        if (!favorite && secondarySlime.isBound()) favorite = entityType == secondarySlime.get().favoriteEntity();
        return favorite;
    }

    public int getEatingCooldown() {
        return this.entityData.get(EATING_COOLDOWN);
    }

    public void setTarred() {
        this.tarred = true;
    }

    public boolean getTarred() {
        return this.tarred;
    }

    public void setItemTarget(ItemEntity entity) {
        this.itemTarget = entity;
    }

    public ItemEntity getItemTarget() {
        return this.itemTarget;
    }

    public void setHappiness(int data) {
        this.entityData.set(HAPPINESS, data);
    }

    public int getHappiness() {
        return this.entityData.get(HAPPINESS);
    }

    public void setEatingCooldown(int data) {
        this.entityData.set(EATING_COOLDOWN, data);
    }

    private boolean hasTrait(String trait) {
        return false;
    }

    public void push(Entity pEntity) {
        super.push(pEntity);
        if (notHungry()) return;
        List<EntityType<? extends LivingEntity>> edibleMobs = getEdibleMobs();
        if (edibleMobs == null) return;
        for (EntityType mobType : edibleMobs) {
            if (pEntity.getType() == mobType && this.isDealsDamage()) {
                this.dealDamage((LivingEntity) pEntity);
                DynamicHolder<SlimeBreed> slime = getSlime();
                applyNegativeEffects((LivingEntity) pEntity, slime);
                if (((LivingEntity) pEntity).getHealth() <= 0 && ((LivingEntity) pEntity).getKillCredit() == this) {
                    handleFeed(getFavoriteEntity((EntityType<? extends LivingEntity>) pEntity.getType()));
                }
            }
        }
    }

    public void playerTouch(Player pEntity) {
        super.playerTouch(pEntity);
        if (this.isDealsDamage()) {
            DynamicHolder<SlimeBreed> slime = getSlime();
            applyNegativeEffects(pEntity, slime);
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
        this.heal(2);
        this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 0.9F);
        setHappiness(getHappiness() + 10);
        setEatingCooldown(SLIME_EAT_COOLDOWN);
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
                    } else {
                        causeChaos();
                        causeChaos();
                    }
                }
            }
            if (!atePlort) handleFeed(isFavoriteFood(item.getItem()));
            item.setCount(item.getCount() - 1);
            itemEntity.setItem(item);
        }
    }

    public void causeChaos() {
        this.setTarred();
        this.kill();
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        int size = this.getSize();
        if (!this.level().isClientSide && this.isDeadOrDying()) {
            if (!this.getTarred()) {
                if (size > 1) {
                    Component component = this.getCustomName();
                    boolean flag = this.isNoAi();
                    SlimeEntityBase slime = (SlimeEntityBase) this.getType().create(this.level());
                    if (slime != null) {
                        if (this.isPersistenceRequired()) {
                            slime.setPersistenceRequired();
                        }
                        String secondaryBreed = this.getSlimeSecondaryBreed();
                        slime.setSlimeBreed(this.getSlimeBreed());
                        if (!secondaryBreed.isEmpty()) slime.setSlimeSecondaryBreed(secondaryBreed);
                        slime.setCustomName(component);
                        slime.setNoAi(flag);
                        slime.setInvulnerable(this.isInvulnerable());
                        slime.setSize(size - 1, true);
                        slime.moveTo(this.getX(), this.getY() + (double) 0.5F, this.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
                        this.level().addFreshEntity(slime);
                    }
                }
            } else {
                Component component = this.getCustomName();
                int victimSize = this.getSize();
                if (victimSize > 0) victimSize--;
                SlimeEntityBase tarr = ModElements.Entities.TARR.get().create(this.level());
                tarr.setCustomName(component);
                tarr.setInvulnerable(this.isInvulnerable());
                tarr.setSize(victimSize, true);
                tarr.moveTo(this.getX(), this.getY() + (double) 0.5F, this.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
                this.level().addFreshEntity(tarr);
            }
        }
        this.setRemoved(pReason);
        this.invalidateCaps();
        this.brain.clearMemories();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HAPPINESS, 500);
        this.entityData.define(EATING_COOLDOWN, 0);
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setEatingCooldown(nbt.getInt("EatingCooldown"));
        setHappiness(nbt.getInt("Happiness"));
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("EatingCooldown", getEatingCooldown());
        nbt.putInt("Happiness", getHappiness());
    }

    static class SlimeTargetItemGoal extends Goal {
        private final SplendidSlime slime;
        private ItemEntity targetItem;
        protected final int randomInterval;

        public SlimeTargetItemGoal(SplendidSlime slime, int pRandomInterval) {
            this.slime = slime;
            this.randomInterval = reducedTickDelay(pRandomInterval);
        }

        protected void findTarget() {
            List<ItemEntity> nearbyItems = this.slime.level().getEntitiesOfClass(ItemEntity.class, this.slime.getBoundingBox().inflate(10));
            if (nearbyItems.isEmpty()) return;
            ItemEntity target = null;
            for (ItemEntity potentialTarget : nearbyItems) {
                if (target == null || this.slime.distanceToSqr(potentialTarget) < this.slime.distanceToSqr(target)) {
                    if (slime.wantsToPickUp(potentialTarget.getItem())) target = potentialTarget;
                }
            }
            this.targetItem = target;
            this.slime.setItemTarget(target);
        }


        @Override
        public void tick() {
            if (this.targetItem != null) {
                this.slime.lookAt(targetItem, 10.0F, 10.0F);
                MoveControl movecontrol = this.slime.getMoveControl();
                if (movecontrol instanceof SplendidSlime.SlimeMoveControl slime$slimemovecontrol) {
                    slime$slimemovecontrol.setDirection(this.slime.getYRot(), true);
                }
            }

        }

        @Override
        public boolean canContinueToUse() {
            if (!targetItem.isAlive()) targetItem = null;
            if (this.slime.notHungry()) {
                targetItem = null;
                return false;
            }
            return targetItem != null && targetItem.isAlive();
        }

        @Override
        public boolean canUse() {
            if (this.slime.notHungry()) return false;
            if (this.randomInterval > 0 && this.slime.getRandom().nextInt(this.randomInterval) != 0) {
                return false;
            } else {
                this.findTarget();
                return this.targetItem != null;
            }
        }

        @Override
        public void start() {
            if (this.slime.hasTrait("foodporting")) {
                this.slime.setPos(targetItem.getX(), targetItem.getY(), targetItem.getZ());
            }
            super.start();
        }

        @Override
        public void stop() {
            this.slime.setItemTarget(null);
            this.targetItem = null;
        }

    }

    public class SlimeAttackFoodEntities<T extends LivingEntity> extends TargetGoal {
        protected LivingEntity target;
        protected TargetingConditions targetConditions;
        protected final int randomInterval;

        public SlimeAttackFoodEntities(Mob pMob, boolean pMustSee, int pRandomInterval, @Nullable Predicate<LivingEntity> pTargetPredicate) {
            super(pMob, pMustSee);
            this.randomInterval = reducedTickDelay(pRandomInterval);
            this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(pTargetPredicate);
        }

        protected void findTarget() {
            List<EntityType<? extends LivingEntity>> edibleMobs = getEdibleMobs();
            if (edibleMobs == null) return;
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
            if (notHungry()) return false;
            if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
                return false;
            } else {
                this.findTarget();
                return this.target != null;
            }
        }

        public void start() {
            if (((SplendidSlime) this.mob).hasTrait("foodporting")) {
                this.mob.setPos(target.getX(), target.getY(), target.getZ());
            }
            this.mob.setTarget(this.target);
            super.start();
        }
    }

    static class SlimeAttackGoal extends Goal {
        private final SplendidSlime slime;
        private int growTiredTimer;

        public SlimeAttackGoal(SplendidSlime pSlime) {
            this.slime = pSlime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.slime.getTarget();
            if (livingentity == null) {
                return false;
            } else {
                return !this.slime.canAttack(livingentity) ? false : this.slime.getMoveControl() instanceof SplendidSlime.SlimeMoveControl;
            }
        }

        public void start() {
            this.growTiredTimer = reducedTickDelay(300);
            super.start();
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.slime.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!this.slime.canAttack(livingentity)) {
                return false;
            } else {
                return --this.growTiredTimer > 0;
            }
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = this.slime.getTarget();
            if (livingentity != null) {
                this.slime.lookAt(livingentity, 10.0F, 10.0F);
            }

            MoveControl movecontrol = this.slime.getMoveControl();
            if (movecontrol instanceof SplendidSlime.SlimeMoveControl slime$slimemovecontrol) {
                slime$slimemovecontrol.setDirection(this.slime.getYRot(), true);
            }

        }
    }

    static class SlimeFloatGoal extends Goal {
        private final SplendidSlime slime;

        public SlimeFloatGoal(SplendidSlime pSlime) {
            this.slime = pSlime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            pSlime.getNavigation().setCanFloat(true);
        }

        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof SplendidSlime.SlimeMoveControl;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            if (this.slime.getRandom().nextFloat() < 0.8F) {
                this.slime.getJumpControl().jump();
            }

            MoveControl movecontrol = this.slime.getMoveControl();
            if (movecontrol instanceof SplendidSlime.SlimeMoveControl slime$slimemovecontrol) {
                slime$slimemovecontrol.setWantedMovement(1.2D);
            }

        }
    }

    static class SlimeKeepOnJumpingGoal extends Goal {
        private final SplendidSlime slime;

        public SlimeKeepOnJumpingGoal(SplendidSlime pSlime) {
            this.slime = pSlime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        public void tick() {
            MoveControl movecontrol = this.slime.getMoveControl();
            if (movecontrol instanceof SplendidSlime.SlimeMoveControl slime$slimemovecontrol) {
                slime$slimemovecontrol.setWantedMovement(1.0D);
            }

        }
    }

    static class SlimeMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final SplendidSlime slime;
        private boolean isAggressive;

        public SlimeMoveControl(SplendidSlime pSlime) {
            super(pSlime);
            this.slime = pSlime;
            this.yRot = 180.0F * pSlime.getYRot() / (float) Math.PI;
        }

        public void setDirection(float pYRot, boolean pAggressive) {
            this.yRot = pYRot;
            this.isAggressive = pAggressive;
        }

        public void setWantedMovement(double pSpeed) {
            this.speedModifier = pSpeed;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        public void tick() {
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != Operation.MOVE_TO) {
                this.mob.setZza(0.0F);
            } else {
                this.operation = Operation.WAIT;
                if (this.mob.onGround()) {
                    this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                    if (this.jumpDelay-- <= 0) {
                        this.jumpDelay = this.slime.getRandom().nextInt(20) + 10;
                        if (this.isAggressive) {
                            this.jumpDelay /= 3;
                        }

                        this.slime.getJumpControl().jump();
//                        if (this.slime.doPlayJumpSound()) {
//                            this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getSoundPitch());
//                        }
                    } else {
                        this.slime.xxa = 0.0F;
                        this.slime.zza = 0.0F;
                        this.mob.setSpeed(0.0F);
                    }
                } else {
                    this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }

            }
        }
    }

    static class SlimeRandomDirectionGoal extends Goal {
        private final SplendidSlime slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public SlimeRandomDirectionGoal(SplendidSlime pSlime) {
            this.slime = pSlime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return this.slime.getItemTarget() == null && this.slime.getTarget() == null && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION)) && this.slime.getMoveControl() instanceof SplendidSlime.SlimeMoveControl;
        }

        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
                this.chosenDegrees = (float) this.slime.getRandom().nextInt(360);
            }

            MoveControl movecontrol = this.slime.getMoveControl();
            if (movecontrol instanceof SplendidSlime.SlimeMoveControl slime$slimemovecontrol) {
                slime$slimemovecontrol.setDirection(this.chosenDegrees, false);
            }

        }
    }
}
