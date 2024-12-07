package rearth.oritech.block.blocks.reactor;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.block.entity.reactor.ReactorAbsorberPortEntity;

public class ReactorAbsorberPortBlock extends BaseReactorBlock implements BlockEntityProvider {
    public ReactorAbsorberPortBlock(Settings settings) {
        super(settings);
    }
    
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorAbsorberPortEntity(pos, state);
    }
    
    @Override
    public boolean validForWalls() {
        return true;
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        
        if (!world.isClient && world.getBlockEntity(pos) instanceof ReactorAbsorberPortEntity) {
            var handler = (ExtendedScreenHandlerFactory) world.getBlockEntity(pos);
            player.openHandledScreen(handler);
        }
        
        return ActionResult.SUCCESS;
    }
}
