package rearth.oritech.block.blocks.reactor;

public class ReactorRodBlock extends BaseReactorBlock {
    
    private final int rodCount;
    private final int internalPulseCount;
    
    public ReactorRodBlock(Settings settings, int rodCount, int internalPulseCount) {
        super(settings);
        this.rodCount = rodCount;
        this.internalPulseCount = internalPulseCount;
    }
    
    public int getRodCount() {
        return rodCount;
    }
    
    public int getInternalPulseCount() {
        return internalPulseCount;
    }
}
