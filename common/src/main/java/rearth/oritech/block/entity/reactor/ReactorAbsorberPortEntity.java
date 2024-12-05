package rearth.oritech.block.entity.reactor;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import rearth.oritech.init.BlockEntitiesContent;

public class ReactorAbsorberPortEntity extends BlockEntity {
    public ReactorAbsorberPortEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.REACTOR_ABSORBER_PORT_BLOCK_ENTITY, pos, state);
    }
}
