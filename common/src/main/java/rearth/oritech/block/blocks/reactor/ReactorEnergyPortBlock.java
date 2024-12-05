package rearth.oritech.block.blocks.reactor;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.block.entity.reactor.ReactorEnergyPortEntity;

public class ReactorEnergyPortBlock extends BaseReactorBlock implements BlockEntityProvider {
    public ReactorEnergyPortBlock(Settings settings) {
        super(settings);
    }
    
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorEnergyPortEntity(pos, state);
    }
    
    @Override
    public boolean validForWalls() {
        return true;
    }
}
