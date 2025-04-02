package io.github.chakyl.splendidslimes.blockentity;

import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import io.github.chakyl.splendidslimes.block.PlortRippitBlock;
import io.github.chakyl.splendidslimes.block.SlimeIncubatorBlock;
import io.github.chakyl.splendidslimes.entity.SlimeEntityBase;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlortPressBlockEntity extends BlockEntity implements TickingBlockEntity {
    protected int incubationTime = 0;
    protected String slimeType = "";

    public PlortPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModElements.BlockEntities.SLIME_INCUBATOR.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!slimeType.isEmpty()) {
            if (this.incubationTime >= 200) {
                SlimeEntityBase birthSlime = ModElements.Entities.SPLENDID_SLIME.get().create(level);
                birthSlime.setSlimeBreed(slimeType);
                birthSlime.setHasSplit(true);
                birthSlime.setSize(1, true);
                BlockPos facingPos = pos.relative(state.getValue(PlortRippitBlock.FACING));
                birthSlime.moveTo(facingPos.getX() + 0.25, facingPos.getY(), facingPos.getZ() + 0.25, level.random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(birthSlime);
                BlockState newState = state.setValue(SlimeIncubatorBlock.WORKING, false);
                level.setBlockAndUpdate(pos, newState);
                this.slimeType = "";
                setChanged();
            }
            else {
                this.incubationTime++;
            }
        }
        else this.incubationTime = 0;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("incubationTime", this.incubationTime);
        tag.putString("slimeType", this.slimeType);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.incubationTime = tag.getInt("incubationTime");
        this.slimeType = tag.getString("slimeType");
    }

    public void setSlimeType(String type) {
        this.slimeType = type;
    }

    public String getSlimeType() {
        return this.slimeType;
    }

    public int getIncubationTime() {
        return this.incubationTime;
    }

}