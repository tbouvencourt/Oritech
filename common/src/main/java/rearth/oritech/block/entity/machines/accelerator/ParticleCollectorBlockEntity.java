package rearth.oritech.block.entity.machines.accelerator;

import earth.terrarium.common_storage_lib.energy.EnergyProvider;
import earth.terrarium.common_storage_lib.storage.base.ValueStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.init.BlockEntitiesContent;
import rearth.oritech.util.DynamicEnergyStorage;

public class ParticleCollectorBlockEntity extends BlockEntity implements BlockEntityTicker<ParticleCollectorBlockEntity>, EnergyProvider.BlockEntity {
    
    // TODO configs
    
    protected final DynamicEnergyStorage energyStorage = new DynamicEnergyStorage(100_000, 0, 0, this::markDirty);
    
    public ParticleCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.PARTICLE_COLLECTOR_BLOCK_ENTITY, pos, state);
    }
    
    @Override
    public ValueStorage getEnergy(@Nullable Direction direction) {
        return energyStorage;
    }
    
    public void onParticleCollided() {
        energyStorage.amount += 5000;
        energyStorage.update();
    }
    
    @Override
    public void tick(World world, BlockPos pos, BlockState state, ParticleCollectorBlockEntity blockEntity) {
        if (world.isClient) return;
        
        // output energy to behind
    }
}
