package io.github.chakyl.splendidslimes.blockentity;

import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.block.SlimeIncubatorBlock;
import io.github.chakyl.splendidslimes.recipe.PlortPressingRecipe;
import io.github.chakyl.splendidslimes.registry.ModElements;
import io.github.chakyl.splendidslimes.screen.PlortPressMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class PlortPressBlockEntity extends BlockEntity implements TickingBlockEntity, MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    protected final ContainerData data;
    private int progress = 0;
    private int PRESSING_TIME = 1200;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public PlortPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModElements.BlockEntities.PLORT_PRESS.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> PlortPressBlockEntity.this.progress;
                    case 1 -> PlortPressBlockEntity.this.PRESSING_TIME;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> PlortPressBlockEntity.this.progress = pValue;
                    case 1 -> PlortPressBlockEntity.this.PRESSING_TIME = pValue;
                }
                ;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % 20 == 0) {
            if (hasRecipe()) {
                if (progress == 0) {
                    BlockState newState = state.setValue(SlimeIncubatorBlock.WORKING, true);
                    level.setBlockAndUpdate(pos, newState);
                }
                this.progress = this.progress + 20;
                setChanged(level, pos, state);
                if (this.progress >= PRESSING_TIME) {
                    craftItem();
                    BlockState newState = state.setValue(SlimeIncubatorBlock.WORKING, false);
                    level.setBlockAndUpdate(pos, newState);
                    this.progress = 0;
                }
            } else {
                this.progress = 0;
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PlortPressMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    private void craftItem() {
        Optional<PlortPressingRecipe> recipe = getCurrentRecipe();
        ItemStack result = recipe.get().getResultItem(null);
        int resultCount = result.getCount();

        ItemStack outputSlot = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        this.itemHandler.extractItem(INPUT_SLOT, recipe.get().getInputItem(null).getCount(), false);
        if (outputSlot.getCount() > 0 && canInsertItemIntoOutputSlot(outputSlot, result) && canInsertAmountIntoOutputSlot(outputSlot, resultCount)) {
            outputSlot.setCount(outputSlot.getCount() + resultCount);
        } else {
            this.itemHandler.setStackInSlot(OUTPUT_SLOT, result.copy());
        }

    }

    private boolean hasRecipe() {
        Optional<PlortPressingRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return false;
        ItemStack input = recipe.get().getInputItem(getLevel().registryAccess());
        ItemStack output = recipe.get().getOutputItem(getLevel().registryAccess());
        ItemStack result = recipe.get().getResultItem(getLevel().registryAccess());
        ItemStack inputSlot = this.itemHandler.getStackInSlot(INPUT_SLOT);
        ItemStack outputSlot = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        if ((output.isEmpty() && (slotMatches(outputSlot, result) && (canInsertAmountIntoOutputSlot(outputSlot, result.getCount()) && canInsertItemIntoOutputSlot(outputSlot, result)))) || validateFusion(input, inputSlot, output, outputSlot)) {
            return true;
        }
        return false;
    }

    private boolean slotMatches(ItemStack slot, ItemStack recipe) {
        return slot.is(recipe.getItem()) && slot.areShareTagsEqual(recipe);
    }

    private boolean validateFusion(ItemStack input, ItemStack inputSlot, ItemStack output, ItemStack outputSlot) {

        return slotMatches(inputSlot, input) && slotMatches(outputSlot, output) && outputSlot.getCount() == output.getCount();
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output, ItemStack result) {
        return output.isEmpty() || (output.is(result.getItem()) && output.areShareTagsEqual(result));
    }

    private boolean canInsertAmountIntoOutputSlot(ItemStack output, int addedCount) {
        return output.getCount() + addedCount <= output.getMaxStackSize();
    }

    private Optional<PlortPressingRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        return this.level.getRecipeManager().getRecipeFor(PlortPressingRecipe.Type.INSTANCE, inventory, level);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("plort_press.progress", progress);
        super.saveAdditional(tag);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("plort_press.progress");
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("block.splendid_slimes.plort_press");
    }

}