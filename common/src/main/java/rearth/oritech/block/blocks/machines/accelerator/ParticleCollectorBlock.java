package rearth.oritech.block.blocks.machines.accelerator;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.block.entity.machines.accelerator.ParticleCollectorBlockEntity;

import java.util.Objects;

public class ParticleCollectorBlock extends FacingBlock implements BlockEntityProvider {
    
    public ParticleCollectorBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FacingBlock.FACING, Direction.NORTH));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FacingBlock.FACING);
    }
    
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return Objects.requireNonNull(super.getPlacementState(ctx)).with(FacingBlock.FACING, ctx.getPlayerLookDirection().getOpposite());
    }
    
    @Override
    protected MapCodec<? extends FacingBlock> getCodec() {
        return null;
    }
    
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ParticleCollectorBlockEntity(pos, state);
    }
    

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof BlockEntityTicker ticker)
                ticker.tick(world1, pos, state1, blockEntity);
        };
    }
    
//    @Override
//    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
//        tooltip.add(Text.translatable("tooltip.oritech.black_hole").formatted(Formatting.GOLD));
//    }
}
