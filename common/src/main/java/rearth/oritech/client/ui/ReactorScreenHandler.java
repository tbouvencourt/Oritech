package rearth.oritech.client.ui;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import rearth.oritech.block.entity.reactor.ReactorControllerBlockEntity;
import rearth.oritech.client.init.ModScreens;

public class ReactorScreenHandler extends ScreenHandler {
    
    public final ReactorControllerBlockEntity reactorEntity;
    public final World world;
    
    // this calls the second version
    public ReactorScreenHandler(int syncId, PlayerInventory inventory, ModScreens.BasicData setupData) {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(setupData.pos()));
    }
    
    // on server, also called from client constructor
    public ReactorScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity) {
        super(ModScreens.REACTOR_SCREEN, syncId);
        
        reactorEntity = (ReactorControllerBlockEntity) blockEntity;
        world = blockEntity.getWorld();
    }
    
    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }
    public boolean canUse(PlayerEntity player) {
        return true;
    }
    
    public static class HandlerFactory implements ExtendedScreenHandlerType.ExtendedFactory<ReactorScreenHandler, ModScreens.BasicData> {
        @Override
        public ReactorScreenHandler create(int syncId, PlayerInventory inventory, ModScreens.BasicData data) {
            return new ReactorScreenHandler(syncId, inventory, data);
        }
    }
}
