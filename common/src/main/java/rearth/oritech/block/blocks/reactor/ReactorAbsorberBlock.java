package rearth.oritech.block.blocks.reactor;

import net.minecraft.block.Block;
import rearth.oritech.init.BlockContent;

public class ReactorAbsorberBlock extends BaseReactorBlock {
    public ReactorAbsorberBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public Block requiredStackCeiling() {
        return BlockContent.REACTOR_ABSORBER_PORT;
    }
}
