package rearth.oritech.block.entity.reactor;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import rearth.oritech.block.blocks.reactor.*;
import rearth.oritech.client.init.ModScreens;
import rearth.oritech.client.ui.ReactorScreenHandler;
import rearth.oritech.init.BlockEntitiesContent;
import rearth.oritech.network.NetworkContent;
import rearth.oritech.util.energy.EnergyApi;
import rearth.oritech.util.energy.containers.SimpleEnergyStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ReactorControllerBlockEntity extends BlockEntity implements BlockEntityTicker<ReactorControllerBlockEntity>, EnergyApi.BlockProvider, ExtendedScreenHandlerFactory {
    
    public static final int MAX_SIZE = 64;
    
    private final HashMap<Vector2i, BaseReactorBlock> activeComponents = new HashMap<>();
    private final HashMap<Vector2i, Integer> componentHeats = new HashMap<>();
    private final HashMap<Vector2i, ComponentStatistics> componentStats = new HashMap<>(); // mainly for client displays
    
    public boolean active = false;
    private int reactorHeat;   // the heat of the entire casing
    private int reactorStackHeight;
    private BlockPos areaMin;
    private BlockPos areaMax;
    private SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(0, 1_000_000, 10_000_000, this::markDirty);
    
    // client only
    public NetworkContent.ReactorUIDataPacket uiData;
    public NetworkContent.ReactorUISyncPacket uiSyncData;
    
    public ReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesContent.REACTOR_CONTROLLER_BLOCK_ENTITY, pos, state);
    }
    
    // heat is only used for reactor rods and heat pipes (and the casing itself)
    // rods generate heat. Base rate pushes it to reactor at generation speed. Multi-cores and reflectors (expensive) change this
    // heat pipes move heat to them and to the reactor casing
    // vents remove heat from the reactor casing
    
    @Override
    public void tick(World world, BlockPos pos, BlockState state, ReactorControllerBlockEntity blockEntity) {
        if (world.isClient || activeComponents.isEmpty()) return;
        
        for (var localPos : activeComponents.keySet()) {
            var component = activeComponents.get(localPos);
            var componentHeat = componentHeats.get(localPos);
            if (component instanceof ReactorRodBlock rodBlock) {
                
                var ownRodCount = rodBlock.getRodCount();
                var receivedPulses = rodBlock.getInternalPulseCount();
                
                // check how many pulses are received from neighbors / reflectors
                for (var neighborPos : getNeighborsInBounds(localPos, activeComponents.keySet())) {
                    
                    var neighbor = activeComponents.get(neighborPos);
                    if (neighbor instanceof ReactorRodBlock neighborRod) {
                        receivedPulses += neighborRod.getRodCount();
                    } else if (neighbor instanceof ReactorReflectorBlock reflectorBlock) {
                        receivedPulses += rodBlock.getRodCount();
                    }
                }
                
                // generate 5 RF per pulse
                energyStorage.insertIgnoringLimit(5 * receivedPulses * reactorStackHeight, false);
                
                // generate heat per pulse
                var heatCreated = (receivedPulses / 2 * receivedPulses + 4);
                componentHeat += heatCreated;
                
                // move a base amount of heat to the reactor hull
                var moved = ownRodCount * 4;
                componentHeat -= moved;
                reactorHeat += moved * reactorStackHeight;
                
                componentStats.put(localPos, new ComponentStatistics((short) receivedPulses, componentHeat, (short) heatCreated, (short) (moved)));
                
            } else if (component instanceof ReactorHeatPipeBlock heatPipeBlock) {
                
                var sumGainedHeat = 0;
                
                // take heat in from neighbors
                for (var neighbor : getNeighborsInBounds(localPos, activeComponents.keySet())) {
                    var neighborHeat = componentHeats.get(neighbor);
                    if (neighborHeat <= componentHeat) continue;
                    var diff = neighborHeat - componentHeat;
                    var gainedHeat = diff / 100;
                    neighborHeat -= gainedHeat;
                    componentHeats.put(neighbor, neighborHeat);
                    componentHeat += gainedHeat;
                    sumGainedHeat += gainedHeat;
                }
                
                // move heat to reactor
                var moveAmount = Math.min(12, componentHeat);
                reactorHeat += moveAmount * reactorStackHeight;
                componentHeat -= moveAmount;
                
                componentStats.put(localPos, new ComponentStatistics((short) 0, componentHeat, (short) sumGainedHeat, (short) (moveAmount)));
                
            } else if (component instanceof ReactorAbsorberBlock absorberBlock) {
                
                var sumRemovedHeat = 0;
                
                // take heat in from neighbors and remove it
                for (var neighbor : getNeighborsInBounds(localPos, activeComponents.keySet())) {
                    var neighborHeat = componentHeats.get(neighbor);
                    if (neighborHeat <= 0) continue;
                    neighborHeat -= 6;
                    sumRemovedHeat += 6;
                    componentHeats.put(neighbor, neighborHeat);
                }
                
                componentStats.put(localPos, new ComponentStatistics((short) 0, componentHeat, (short) sumRemovedHeat, (short) (0)));
            } else if (component instanceof ReactorHeatVentBlock ventBlock) {
                
                var newHeat = (reactorHeat - 6 * reactorStackHeight);
                reactorHeat = Math.max(newHeat, 0);
                
                componentStats.put(localPos, new ComponentStatistics((short) 0, componentHeat, (short) 6, (short) 0));
                
            }
            
            componentHeats.put(localPos, componentHeat);
        }
        
        sendUINetworkData();
        
    }
    
    public void init(PlayerEntity player) {
        
        active = false;
        
        // find low and high corners of reactor
        var cornerA = pos;
        cornerA = expandWall(cornerA, new Vec3i(0, -1, 0), true);   // first go down through other wall blocks
        cornerA = expandWall(cornerA, new Vec3i(0, 0, -1));
        cornerA = expandWall(cornerA, new Vec3i(-1, 0, 0));
        cornerA = expandWall(cornerA, new Vec3i(0, 0, -1)); // expand z again to support all rotations
        
        var cornerB = cornerA;
        cornerB = expandWall(cornerB, new Vec3i(0, 1, 0));
        cornerB = expandWall(cornerB, new Vec3i(0, 0, 1));
        cornerB = expandWall(cornerB, new Vec3i(1, 0, 0));
        
        if (cornerA == pos || cornerB == pos || cornerA == cornerB || onSameAxis(cornerA, cornerB)) {
            player.sendMessage(Text.translatable("message.oritech.reactor_invalid"));
            return;
        }
        
        System.out.println("corners valid");
        
        // verify and load all blocks in reactor area
        var finalCornerA = cornerA;
        var finalCornerB = cornerB;
        
        // verify edges
        var wallsValid = BlockPos.stream(cornerA, cornerB).allMatch(pos -> {
            if (isAtEdgeOfBox(pos, finalCornerA, finalCornerB)) {
                var block = world.getBlockState(pos).getBlock();
                return block instanceof ReactorWallBlock;
            } else if (isOnWall(pos, finalCornerA, finalCornerB)) {
                var block = world.getBlockState(pos).getBlock();
                return !(block instanceof BaseReactorBlock reactorBlock) || reactorBlock.validForWalls();
            }
            
            return true;
        });
        
        if (!wallsValid) {
            player.sendMessage(Text.translatable("message.oritech.reactor_invalid"));
            return;
        }
        
        // verify interior is identical in all layers
        var interiorHeight = cornerB.getY() - cornerA.getY() - 1;
        var cornerAFlat = cornerA.add(1, 1, 1);
        var cornerBFlat = new BlockPos(cornerB.getX() - 1, cornerA.getY() + 1, cornerB.getZ() - 1);
        
        activeComponents.clear();
        reactorStackHeight = interiorHeight;
        
        var interiorStackedRight = BlockPos.stream(cornerAFlat, cornerBFlat).allMatch(pos -> {
            
            var block = world.getBlockState(pos).getBlock();
            if (!(block instanceof BaseReactorBlock reactorBlock)) return true;
            
            for (int i = 1; i < interiorHeight; i++) {
                var candidatePos = pos.add(0, i, 0);
                var candidate = world.getBlockState(candidatePos);
                if (!candidate.getBlock().equals(block))
                    return false;
            }
            
            var requiredCeiling = reactorBlock.requiredStackCeiling();
            if (requiredCeiling != Blocks.AIR) {
                var ceilingPos = pos.add(0, interiorHeight, 0);
                System.out.println("ceiling need: " + requiredCeiling + " got: " + world.getBlockState(ceilingPos).getBlock());
                if (!requiredCeiling.equals(world.getBlockState(ceilingPos).getBlock())) return false;
            }
            
            var offset = pos.subtract(cornerAFlat);
            var localPos = new Vector2i(offset.getX(), offset.getZ());
            activeComponents.put(localPos, reactorBlock);
            componentHeats.put(localPos, 0);
            System.out.println(offset + ": " + reactorBlock);
            
            return true;
        });
        
        if (!interiorStackedRight) {
            player.sendMessage(Text.translatable("message.oritech.interior_invalid"));
            return;
        }
        
        areaMin = finalCornerA;
        areaMax = finalCornerB;
        active = true;
        
        System.out.println("walls: " + wallsValid + " interiorStack: " + interiorStackedRight + " height: " + interiorHeight);
        
    }
    
    private static Set<Vector2i> getNeighborsInBounds(Vector2i pos, Set<Vector2i> keys) {
        
        var res = new HashSet<Vector2i>(4);
        
        var a = new Vector2i(pos).add(-1, 0);
        if (keys.contains(a)) res.add(a);
        var b = new Vector2i(pos).add(0, 1);
        if (keys.contains(b)) res.add(b);
        var c = new Vector2i(pos).add(1, 0);
        if (keys.contains(c)) res.add(c);
        var d = new Vector2i(pos).add(0, -1);
        if (keys.contains(d)) res.add(d);
        
        return res;
    }
    
    private static boolean onSameAxis(BlockPos A, BlockPos B) {
        return A.getX() == B.getX() || A.getY() == B.getY() || A.getZ() == B.getZ();
    }
    
    private static boolean isOnWall(BlockPos pos, BlockPos min, BlockPos max) {
        return onSameAxis(pos, min) || onSameAxis(pos, max);
    }
    
    private static boolean isAtEdgeOfBox(BlockPos pos, BlockPos min, BlockPos max) {
        int planesAligned = 0;
        
        if (pos.getX() == min.getX() || pos.getX() == max.getX()) planesAligned++;
        if (pos.getY() == min.getY() || pos.getY() == max.getY()) planesAligned++;
        if (pos.getZ() == min.getZ() || pos.getZ() == max.getZ()) planesAligned++;
        
        return planesAligned >= 2;
    }
    
    private BlockPos expandWall(BlockPos from, Vec3i direction) {
        return expandWall(from, direction, false);
    }
    
    private BlockPos expandWall(BlockPos from, Vec3i direction, boolean allReactorBlocks) {
        
        var result = from;
        for (int i = 1; i < MAX_SIZE; i++) {
            var candidate = from.add(direction.multiply(i));
            var candidateBlock = world.getBlockState(candidate).getBlock();
            
            if (!allReactorBlocks && !(candidateBlock instanceof ReactorWallBlock)) return result;
            if (allReactorBlocks && !(candidateBlock instanceof BaseReactorBlock)) return result;
            
            result = candidate;
        }
        
        return result;
        
    }
    
    @Override
    public EnergyApi.EnergyContainer getStorage(Direction direction) {
        return energyStorage;
    }
    
    private void sendUINetworkData() {
        
        if (activeComponents.isEmpty()) return;
        
        var positionsFlat = activeComponents.keySet();
        var positions = positionsFlat.stream().map(pos -> areaMin.add(pos.x + 1, 1, pos.y + 1)).toList();
        var heats = positionsFlat.stream().map(pos -> componentStats.getOrDefault(pos, ComponentStatistics.EMPTY)).toList();
        
        NetworkContent.MACHINE_CHANNEL.serverHandle(this).send(new NetworkContent.ReactorUISyncPacket(pos, positions, heats));
    }
    
    @Override
    public Object getScreenOpeningData(ServerPlayerEntity player) {
        var previewMax = new BlockPos(areaMax.getX(), areaMin.getY() + 1, areaMax.getZ());
        NetworkContent.MACHINE_CHANNEL.serverHandle(this).send(new NetworkContent.ReactorUIDataPacket(pos, areaMin, areaMax, previewMax));
        return new ModScreens.BasicData(pos);
    }
    
    @Override
    public Text getDisplayName() {
        return Text.of("");
    }
    
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ReactorScreenHandler(syncId, playerInventory, this);
    }
    
    public record ComponentStatistics(short receivedPulses, int storedHeat, short heatChanged, short heatToReactor) {
        public static final ComponentStatistics EMPTY = new ComponentStatistics((short) 0, -1, (short) 0, (short) 0);
    }
}
