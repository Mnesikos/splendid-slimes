package io.github.chakyl.splendidslimes.entity;

import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Tarr extends SlimeEntityBase {
    public static final String SLIME = "slime";
    public static final String ID = "id";
    public static final String DATA = "data";
    private final EntityType<SlimeEntityBase> entityType;

    public Tarr(EntityType<SlimeEntityBase> entityType, Level level) {
        super(entityType, level);
        this.entityType = entityType;
    }

    protected void registerGoals() {
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, SplendidSlime.class, true));
        super.registerGoals();

    }

    public EntityType<SlimeEntityBase> getEntityType() {
        return entityType;
    }

    @Override
    protected @NotNull Component getTypeName() {
        return Component.translatable("entity.splendid_slimes.tarr");
    }


    public void push(Entity pEntity) {
        super.push(pEntity);
        if (pEntity instanceof SplendidSlime && this.isDealsDamage()) {
            this.dealDamage((LivingEntity) pEntity);
            if (((LivingEntity) pEntity).getHealth() <= 0 && ((LivingEntity) pEntity).getKillCredit() == this) {
                tarrReplicate(pEntity);
            }
        }
    }

    private void tarrReplicate(Entity victim) {
        int tarrSize = this.getSize();
        if (tarrSize < 4) this.setSize(this.getSize() + 1, true);
        ((SplendidSlime) victim).setTarred();

    }

    @Override
    protected ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, ModElements.Items.TARRTAR.get().getDefaultInstance());
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        this.setRemoved(pReason);
        this.invalidateCaps();
        this.brain.clearMemories();
    }
}
