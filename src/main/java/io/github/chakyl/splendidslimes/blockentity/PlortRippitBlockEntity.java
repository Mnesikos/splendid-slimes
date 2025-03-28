package io.github.chakyl.splendidslimes.blockentity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.block.PlortRippitBlock;
import io.github.chakyl.splendidslimes.block.SlimeIncubatorBlock;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.entity.SlimeEntityBase;
import io.github.chakyl.splendidslimes.item.PlortItem;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import oshi.hardware.SoundCard;

import java.util.List;

import static io.github.chakyl.splendidslimes.block.PlortRippitBlock.WORKING;
import static io.github.chakyl.splendidslimes.util.SlimeData.getSlimeData;

public class PlortRippitBlockEntity extends BlockEntity implements TickingBlockEntity {
    protected int processingTime = 0;
    protected String slimeType = "";
    protected final RippitItemHandler inventory = new RippitItemHandler();

    public PlortRippitBlockEntity(BlockPos pos, BlockState state) {
        super(ModElements.BlockEntities.PLORT_RIPPIT.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!inventory.getStackInSlot(0).isEmpty()) {
            if (this.processingTime == 0 && !state.getValue(WORKING)) {
                BlockState newState = state.setValue(WORKING, true);
                level.setBlock(pos, newState, 2);
                this.setSlimeType(inventory.getStackInSlot(0).getTagElement("plort").get("id").toString().replace("\"", ""));
                level.playSound(null, pos, SoundEvents.FROG_TONGUE, SoundSource.BLOCKS, 1.0F, 0.9F);
                level.playSound(null, pos, SoundEvents.FROG_EAT, SoundSource.BLOCKS, 1.0F, 0.9F);
            } else if (this.processingTime == 100) {
                DynamicHolder<SlimeBreed> slime = getSlimeData(slimeType);
                BlockState newState = state.setValue(WORKING, false);
                level.setBlockAndUpdate(pos, newState);
                inventory.getStackInSlot(0).shrink(1);
                this.slimeType = "";
                if (slime.isBound()) {
                    List<ItemStack> plortResources = slime.get().plortResources();
                    for (ItemStack item : plortResources) {
                        Block.popResourceFromFace(level, pos, state.getValue(PlortRippitBlock.FACING).getOpposite(), new ItemStack(item.getItem()));
                        level.playSound(null, pos, SoundEvents.FROG_AMBIENT, SoundSource.BLOCKS, 0.7F, 0.95F + level.getRandom().nextFloat() * 0.1F);
                    }
                }
                setChanged();
            }
            else {
                this.processingTime++;
            }
        }
        else this.processingTime = 0;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", this.inventory.serializeNBT());
        tag.putInt("processingTime", this.processingTime);
        tag.putString("slimeType", this.slimeType);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound("inventory"));
        this.processingTime = tag.getInt("processingTime");
        this.slimeType = tag.getString("slimeType");
    }

    public void setSlimeType(String type) {
        this.slimeType = type;
    }

    public String getSlimeType() {
        return this.slimeType;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }
    public boolean insertItem(ItemStack itemStack) {
        if (inventory.isItemValid(0, itemStack)) {
            ItemStack modifiedStack = itemStack.copy();
            modifiedStack.setCount(1);
            inventory.setStackInSlot(0, modifiedStack);
            return true;
        };
        return false;
    }
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return LazyOptional.of(() -> this.inventory).cast();
        return super.getCapability(cap, side);
    }

    public class RippitItemHandler extends InternalItemHandler {

        public RippitItemHandler() {
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (this.getStackInSlot(0).isEmpty() && stack.getItem() instanceof PlortItem && stack.hasTag()) {
                CompoundTag plortTag = stack.getTagElement("plort");
                return plortTag != null && plortTag.contains("id");
            };
            return false;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return super.insertItem(slot, stack, simulate);
        }


        @Override
        protected void onContentsChanged(int slot) {
            PlortRippitBlockEntity.this.setChanged();
        }

    }
}