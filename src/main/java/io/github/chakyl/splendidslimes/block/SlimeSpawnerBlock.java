package io.github.chakyl.splendidslimes.block;

import dev.shadowsoffire.placebo.block_entity.TickingEntityBlock;
import io.github.chakyl.splendidslimes.blockentity.SlimeIncubatorBlockEntity;
import io.github.chakyl.splendidslimes.blockentity.SlimeSpawnerBlockEntity;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SlimeSpawnerBlock extends HorizontalDirectionalBlock implements TickingEntityBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public SlimeSpawnerBlock(Properties props) {
        super(props);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        pBuilder.add(OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(OPEN, false);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SlimeSpawnerBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && pPlayer.isCreative()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (pHand == InteractionHand.MAIN_HAND && entity instanceof SlimeSpawnerBlockEntity) {
                ItemStack heldItem = pPlayer.getItemInHand(pHand);
                if (heldItem.getItem() == ModElements.Items.SPLENDID_SLIME_SPAWN_EGG.get() && heldItem.hasTag()) {
                    CompoundTag plortTag = heldItem.getTagElement("EntityTag");
                    if (plortTag != null && plortTag.contains("Breed")) {
                        if (!pPlayer.isCreative()) heldItem.shrink(1);

                        BlockState newState = pState.setValue(OPEN, false);
                        pLevel.setBlock(pPos, newState, 2);

                        pLevel.playSound(pPlayer, pPos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        ((SlimeSpawnerBlockEntity) entity).setSlimeType(plortTag.get("Breed").toString().replace("\"", ""));
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.FAIL;
        } else {
            return InteractionResult.SUCCESS;
        }
    }
}