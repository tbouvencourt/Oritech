package rearth.oritech.block.blocks.reactor;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public abstract class BaseReactorBlock extends Block {
    
    public BaseReactorBlock(Settings settings) {
        super(settings);
    }
    
    public boolean validForWalls() { return false; }
    
    public Block requiredStackCeiling() {
        return Blocks.AIR;
    }
    
}
