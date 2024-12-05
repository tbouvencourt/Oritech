package rearth.oritech.block.entity.reactor;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import rearth.oritech.init.BlockEntitiesContent;

public class ReactorFuelPortEntity extends BlockEntity {
    public ReactorFuelPortEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.REACTOR_FUEL_PORT_BLOCK_ENTITY, pos, state);
    }
}
