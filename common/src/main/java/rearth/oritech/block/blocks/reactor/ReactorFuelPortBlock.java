package rearth.oritech.block.blocks.reactor;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.block.entity.reactor.ReactorFuelPortEntity;

public class ReactorFuelPortBlock extends BaseReactorBlock implements BlockEntityProvider {
    public ReactorFuelPortBlock(Settings settings) {
        super(settings);
    }
    
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorFuelPortEntity(pos, state);
    }
    
    @Override
    public boolean validForWalls() {
        return true;
    }
}
