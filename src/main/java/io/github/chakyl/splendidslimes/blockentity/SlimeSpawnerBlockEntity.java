package io.github.chakyl.splendidslimes.blockentity;

import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import io.github.chakyl.splendidslimes.block.PlortRippitBlock;
import io.github.chakyl.splendidslimes.block.SlimeIncubatorBlock;
import io.github.chakyl.splendidslimes.block.SlimeSpawnerBlock;
import io.github.chakyl.splendidslimes.entity.SlimeEntityBase;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlimeSpawnerBlockEntity extends BlockEntity implements TickingBlockEntity {
    private int cooldown = 0;
    private int SPAWNER_COOLDOWN = 2000;
    private int ACTIVATION_RANGE = 10;
    protected String slimeType = "";


    public SlimeSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModElements.BlockEntities.SLIME_SPAWNER.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!slimeType.isEmpty() && isNearPlayer(level, pos)) {
            if (this.cooldown == 0) {
                SlimeEntityBase birthSlime = ModElements.Entities.SPLENDID_SLIME.get().create(level);
                birthSlime.setSlimeBreed(slimeType);
                birthSlime.setSize(1, true);
                birthSlime.setPersistenceRequired();
                birthSlime.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(birthSlime);
                birthSlime.push(0, 2, 0);
                BlockState newState = state.setValue(SlimeSpawnerBlock.OPEN, true);
                level.setBlockAndUpdate(pos, newState);
                setChanged();
            } else {
                this.cooldown++;
            }
        } else this.cooldown = 0;
    }
    private boolean isNearPlayer(Level pLevel, BlockPos pPos) {
        return pLevel.hasNearbyAlivePlayer((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, (double)this.ACTIVATION_RANGE);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("cooldown", this.cooldown);
        tag.putString("slimeType", this.slimeType);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.cooldown = tag.getInt("cooldown");
        this.slimeType = tag.getString("slimeType");
    }

    public void setSlimeType(String type) {
        this.slimeType = type;
    }

    public String getSlimeType() {
        return this.slimeType;
    }

    public int getcooldown() {
        return this.cooldown;
    }
}