package io.github.chakyl.splendidslimes.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;


public class SplendidSlime extends SlimeEntityBase {
    public static final String SLIME = "slime";
    public static final String ID = "id";
    public static final String DATA = "data";
    private final EntityType<SlimeEntityBase> entityType;

    public SplendidSlime(EntityType<SlimeEntityBase> entityType, Level level) {
        super(entityType, level);
        this.entityType = entityType;
    }

    public EntityType<SlimeEntityBase> getEntityType() {
        return entityType;
    }


//    @Override
//    protected ParticleOptions getParticleType() {
//        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(this.growthItem));
//
//    }
}
