package rearth.oritech.util;

import net.minecraft.item.ItemStack;

public class SimpleEnergyItemStorage extends SimpleEnergyStorage {
    
    private final ItemStack stack;
    
    public SimpleEnergyItemStorage(long maxInsert, long maxExtract, long capacity, ItemStack stack) {
        super(maxInsert, maxExtract, capacity);
        this.stack = stack;
        this.setAmount(stack.getOrDefault(EnergyApi.ITEM.getEnergyComponent(), 0L));
    }
    
    @Override
    public void update() {
        super.update();
        stack.set(EnergyApi.ITEM.getEnergyComponent(), getAmount());
    }
}
