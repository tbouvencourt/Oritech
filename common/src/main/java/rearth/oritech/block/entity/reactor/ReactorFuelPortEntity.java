package rearth.oritech.block.entity.reactor;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.client.init.ModScreens;
import rearth.oritech.client.ui.BasicMachineScreenHandler;
import rearth.oritech.init.BlockEntitiesContent;
import rearth.oritech.network.NetworkContent;
import rearth.oritech.util.*;

import java.util.List;

public class ReactorFuelPortEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ScreenProvider, InventoryProvider {
    
    private final SimpleSidedInventory inventory = new SimpleSidedInventory(2, new InventorySlotAssignment(0, 1, 1, 0));
    private final InventoryStorage inventoryStorage = InventoryStorage.of(inventory, null);
    
    public int availableFuel;
    public int currentFuelOriginalCapacity;
    
    public ReactorFuelPortEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.REACTOR_FUEL_PORT_BLOCK_ENTITY, pos, state);
    }
    
    public boolean tryConsumeFuel(int amount) {
        if (availableFuel >= amount) {
            availableFuel -= amount;
            return true;
        }
        
        // try consume input
        var inputStack = inventory.getStack(0);
        if (inputStack.isEmpty()) return false;
        
        if (inputStack.getItem().equals(Items.LAPIS_LAZULI)) {
            var capacity = 1000;
            currentFuelOriginalCapacity = capacity;
            availableFuel = capacity - amount;
            inputStack.decrement(1);
            return true;
        }
        
        return false;
        
    }
    
    public void updateNetwork() {
        NetworkContent.MACHINE_CHANNEL.serverHandle(this).send(new NetworkContent.ReactorPortDataPacket(pos, currentFuelOriginalCapacity, availableFuel));
    }
    
    @Override
    public Object getScreenOpeningData(ServerPlayerEntity player) {
        return new ModScreens.BasicData(pos);
    }
    
    @Override
    public Text getDisplayName() {
        return Text.of("");
    }
    
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BasicMachineScreenHandler(syncId, playerInventory, this);
    }
    
    @Override
    public List<GuiSlot> getGuiSlots() {
        return List.of(new GuiSlot(0, 55, 35));
    }
    
    @Override
    public boolean showEnergy() {
        return false;
    }
    
    @Override
    public float getDisplayedEnergyUsage() {
        return 0;
    }
    
    @Override
    public float getProgress() {
        return 0;
    }
    
    @Override
    public boolean showProgress() {
        return false;
    }
    
    @Override
    public InventoryInputMode getInventoryInputMode() {
        return InventoryInputMode.FILL_LEFT_TO_RIGHT;
    }
    
    @Override
    public Inventory getDisplayedInventory() {
        return inventory;
    }
    
    @Override
    public ScreenHandlerType<?> getScreenHandlerType() {
        return ModScreens.FUEL_PORT_SCREEN;
    }
    
    @Override
    public boolean inputOptionsEnabled() {
        return false;
    }
    
    @Override
    public boolean showExpansionPanel() {
        return false;
    }
    
    @Override
    public InventoryStorage getInventory(Direction direction) {
        return inventoryStorage;
    }
}
