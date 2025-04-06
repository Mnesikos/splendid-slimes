package io.github.chakyl.splendidslimes.block;

import dev.shadowsoffire.placebo.block_entity.TickingEntityBlock;
import io.github.chakyl.splendidslimes.blockentity.PlortRippitBlockEntity;
import io.github.chakyl.splendidslimes.item.PlortItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PlortRippitBlock extends HorizontalDirectionalBlock implements TickingEntityBlock {
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    public static final VoxelShape SHAPE = Shapes.or(box(0, 0, 0, 16, 2, 16), box(1, 0, 1, 15, 29, 15));

    public PlortRippitBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, WORKING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(WORKING, false);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PlortRippitBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (pHand == InteractionHand.MAIN_HAND && entity instanceof PlortRippitBlockEntity && !entity.getBlockState().getValue(WORKING)) {
                ItemStack heldItem = pPlayer.getItemInHand(pHand);
                if (!(heldItem.getItem() instanceof PlortItem)) return InteractionResult.FAIL;
                if (!pLevel.isClientSide()) {
                    pPlayer.swing(InteractionHand.MAIN_HAND);
                    if (((PlortRippitBlockEntity) entity).insertItem(heldItem)) {
                        if (!pPlayer.isCreative()) heldItem.shrink(1);
                        return InteractionResult.CONSUME;
                    };
                    return InteractionResult.FAIL;
                } else {
                    pLevel.playSound(pPlayer, pPos, SoundEvents.FROG_TONGUE, SoundSource.BLOCKS, 1.0F, 0.9F);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.FROG_EAT, SoundSource.BLOCKS, 1.0F, 0.9F);
                }
            }
            return InteractionResult.FAIL;
    }
}