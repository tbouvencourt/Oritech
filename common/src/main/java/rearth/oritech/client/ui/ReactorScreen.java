package rearth.oritech.client.ui;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import rearth.oritech.block.blocks.reactor.ReactorAbsorberBlock;
import rearth.oritech.block.blocks.reactor.ReactorHeatPipeBlock;
import rearth.oritech.block.blocks.reactor.ReactorHeatVentBlock;
import rearth.oritech.block.blocks.reactor.ReactorRodBlock;
import rearth.oritech.block.entity.reactor.ReactorControllerBlockEntity;
import rearth.oritech.client.ui.components.ReactorBlockRenderComponent;
import rearth.oritech.client.ui.components.ReactorPreviewContainer;
import rearth.oritech.init.BlockContent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class ReactorScreen extends BaseOwoHandledScreen<FlowLayout, ReactorScreenHandler> {
    
    private ArrayList<Pair<Integer, ReactorBlockRenderComponent>> activeComponents;
    private HashSet<ReactorBlockRenderComponent> activeOverlays;
    private LabelComponent tooltipTitle;
    private FlowLayout tooltipContainer;
    private ReactorBlockRenderComponent selectedBlockOverlay;
    
    public ReactorScreen(ReactorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    
    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }
    
    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
          .surface(Surface.VANILLA_TRANSLUCENT)
          .horizontalAlignment(HorizontalAlignment.CENTER)
          .verticalAlignment(VerticalAlignment.CENTER);
        
        tooltipContainer = Containers.verticalFlow(Sizing.content(2), Sizing.content(2));
        tooltipContainer.surface(Surface.VANILLA_TRANSLUCENT);
        tooltipTitle = Components.label(Text.literal("My title!"));
        tooltipContainer.child(tooltipTitle.margins(Insets.of(6)));
        tooltipContainer.zIndex(3000);
        tooltipContainer.padding(Insets.of(3));
        tooltipTitle.zIndex(3001);
        
        var overlay = Containers.horizontalFlow(Sizing.fixed(290), Sizing.fixed(200));
        rootComponent.child(overlay.surface(Surface.PANEL));
        
        if (handler.reactorEntity.uiData != null) {
            addReactorComponentPreview(overlay);
        }
        
        addTitle(overlay);
        rootComponent.child(tooltipContainer.positioning(Positioning.absolute(0, 0)));
    }
    
    private void addReactorComponentPreview(FlowLayout overlay) {
        
        var holoPreviewContainer = new ReactorPreviewContainer(Sizing.fixed(180), Sizing.fixed(184), FlowLayout.Algorithm.HORIZONTAL, this::onContainerMouseMove);
        holoPreviewContainer.surface(Surface.PANEL_INSET);
        holoPreviewContainer.margins(Insets.of(8));
        
        var uiData = handler.reactorEntity.uiData;
        
        var totalSize = uiData.previewMax().subtract(uiData.min());
        var leftCount = totalSize.getZ();
        var rightCount = totalSize.getX();
        var totalWidth = leftCount + rightCount + 3;
        var middlePercentage = leftCount / (float) totalWidth;
        var xOffset = middlePercentage * 170 + 10;
        
        var size = (int) (170 / (float) totalWidth * 2.2f);
        System.out.println(size);
        
        activeComponents = new ArrayList<>();
        activeOverlays = new HashSet<>();
        
        BlockPos.stream(uiData.min(), uiData.previewMax()).forEach(pos -> {
            var state = handler.world.getBlockState(pos);
            if (state.isAir()) return;
            var offset = pos.subtract(uiData.min());
            var projectedPosX = offset.getX() * 0.43f - offset.getZ() * 0.43f;
            var projectedPosY = offset.getX() * 0.224f + offset.getZ() * 0.224f + offset.getY() * 0.5f;
            var zIndex = offset.getY() - offset.getX() - offset.getZ();
            var preview = new ReactorBlockRenderComponent(handler.world.getBlockState(pos), handler.world.getBlockEntity(pos), zIndex, pos.toImmutable())
                            .sizing(Sizing.fixed(size))
                            .positioning(Positioning.absolute((int) (projectedPosX * size + xOffset), (int) (-projectedPosY * size) + 120));
            if (offset.getY() == 1) {
                activeComponents.add(new Pair<>(-zIndex, (ReactorBlockRenderComponent) preview));
            }
            holoPreviewContainer.child(preview);
            
            if (state.getBlock() instanceof ReactorRodBlock || state.getBlock() instanceof ReactorHeatPipeBlock) {
                var heatOverlay = new ReactorBlockRenderComponent(Blocks.AIR.getDefaultState(), null, zIndex + 0.5f, pos.toImmutable())
                                    .sizing(Sizing.fixed(size))
                                    .positioning(Positioning.absolute((int) (projectedPosX * size + xOffset), (int) (-projectedPosY * size) + 120));
                
                holoPreviewContainer.child(heatOverlay);
                activeOverlays.add((ReactorBlockRenderComponent) heatOverlay);
            }
            
        });
        
        selectedBlockOverlay = (ReactorBlockRenderComponent) new ReactorBlockRenderComponent(Blocks.AIR.getDefaultState(), null, 10 + 0.5f, BlockPos.ORIGIN)
                                     .sizing(Sizing.fixed(size))
                                     .positioning(Positioning.absolute(0, 0));
        holoPreviewContainer.child(selectedBlockOverlay);
        
        activeComponents.sort(Comparator.comparingInt(Pair::getLeft));
        
        overlay.child(holoPreviewContainer);
        
    }
    
    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        
        for (var overlay : activeOverlays) {
            var data = getStatsAtPosition(overlay.pos);
            
            var isEmpty = data.storedHeat() <= 0;
            if (isEmpty) {
                overlay.state = Blocks.AIR.getDefaultState();
                continue;
            }
            
            var res = BlockContent.REACTOR_COLD_INDICATOR_BLOCK.getDefaultState();
            
            // TODO remove magic numbers here
            if (data.storedHeat() > 1000) {
                res = BlockContent.REACTOR_HOT_INDICATOR_BLOCK.getDefaultState();
            } else if (data.storedHeat() > 200) {
                res = BlockContent.REACTOR_MEDIUM_INDICATOR_BLOCK.getDefaultState();
            }
            
            overlay.state = res;
        }
        
    }
    
    private void onContainerMouseMove(int mouseX, int mouseY) {
        
        var posX = mouseX;
        var posY = mouseY;
        
        // check if self is on top of activeComponents
        for (var component : activeComponents) {
            var hit = component.getRight().isInBoundingBox(mouseX, mouseY);
            if (hit) {
                var pos = component.getRight().pos;
                addStatsToTooltip(pos, handler.world.getBlockState(pos), tooltipContainer);
                posX = component.getRight().x();
                posY = component.getRight().y();
                
                selectedBlockOverlay.state = BlockContent.ADDON_INDICATOR_BLOCK.getDefaultState();
                selectedBlockOverlay.pos = pos;
                selectedBlockOverlay.zIndex = component.getRight().zIndex + 0.6f;
                selectedBlockOverlay.positioning(component.getRight().positioning().get());
                
                break;
            }
        }
        
        if (posX == mouseX) {   // move out of visible area
            tooltipContainer.positioning(Positioning.absolute(-100, -500));
            selectedBlockOverlay.state = Blocks.AIR.getDefaultState();
            return;
        }
        
        var containerHeight = tooltipContainer.height();
        
        tooltipContainer.positioning(Positioning.absolute(posX - 30, posY - 5 - containerHeight));
        
    }
    
    public void addStatsToTooltip(BlockPos pos, BlockState state, FlowLayout container) {
        
        container.clearChildren();
        
        var blockname = state.getBlock().getName();
        container.child(Components.label(blockname.formatted(Formatting.WHITE, Formatting.BOLD)).margins(Insets.of(0, 3, 0, 0)));
        
        var stats = getStatsAtPosition(pos);
        if (stats.storedHeat() == -1) return;
        
        // todo remove magic numbers here
        
        if (state.getBlock() instanceof ReactorRodBlock rodBlock) {
            var rodCount = rodBlock.getRodCount();
            var totalPulses = stats.receivedPulses();
            var createdPulses = rodBlock.getInternalPulseCount();
            var externalPulses = totalPulses - createdPulses;
            var generatedEnergy = 5 * totalPulses;
            var generatedHeat = stats.heatChanged();
            var heatToReactor = stats.heatToReactor();
            var heat = stats.storedHeat();
            
            container.child(Components.label(Text.translatable("text.oritech.reactor.rod_count", rodCount).formatted(Formatting.WHITE)));
            container.child(Components.label(Text.translatable("text.oritech.reactor.generated_pulses", createdPulses).formatted(Formatting.WHITE)));
            container.child(Components.label(Text.translatable("text.oritech.reactor.received_pulses", externalPulses).formatted(Formatting.WHITE)));
            container.child(Components.label(Text.translatable("text.oritech.reactor.generated_heat", generatedHeat).formatted(Formatting.WHITE)));
            container.child(Components.label(Text.translatable("text.oritech.reactor.generated_energy", generatedEnergy).formatted(Formatting.WHITE)));
            container.child(Components.label(Text.translatable("text.oritech.reactor.heat_to_reactor", heatToReactor).formatted(Formatting.WHITE)));
            container.child(Components.label(Text.translatable("text.oritech.reactor.heat", heat).formatted(Formatting.WHITE)));
        } else if (state.getBlock() instanceof ReactorHeatPipeBlock pipeBlock) {
            container.child(Components.label(Text.translatable("text.oritech.reactor.collected_heat", stats.heatChanged()).formatted(Formatting.WHITE)));
            container.child(Components.label(Text.translatable("text.oritech.reactor.heat_to_reactor", stats.heatToReactor()).formatted(Formatting.WHITE)));
            container.child(Components.label(Text.translatable("text.oritech.reactor.heat", stats.storedHeat()).formatted(Formatting.WHITE)));
        } else if (state.getBlock() instanceof ReactorHeatVentBlock pipeBlock) {
            container.child(Components.label(Text.translatable("text.oritech.reactor.removed_heat", stats.heatChanged()).formatted(Formatting.WHITE)));
        } else if (state.getBlock() instanceof ReactorAbsorberBlock absorberBlock) {
            container.child(Components.label(Text.translatable("text.oritech.reactor.absorbed_heat", stats.heatChanged()).formatted(Formatting.WHITE)));
        }
        
    }
    
    public ReactorControllerBlockEntity.ComponentStatistics getStatsAtPosition(BlockPos pos) {
        
        if (handler.reactorEntity.uiSyncData == null) return ReactorControllerBlockEntity.ComponentStatistics.EMPTY;
        
        var componentPositions = handler.reactorEntity.uiSyncData.componentPositions();
        for (int i = 0; i < componentPositions.size(); i++) {
            var candidate = componentPositions.get(i);
            if (!candidate.equals(pos)) continue;
            return handler.reactorEntity.uiSyncData.componentHeats().get(i);
        }
        
        return ReactorControllerBlockEntity.ComponentStatistics.EMPTY;
    }
    
    private void addTitle(FlowLayout overlay) {
        var blockTitle = handler.reactorEntity.getCachedState().getBlock().getName();
        var label = Components.label(blockTitle);
        label.color(new Color(64 / 255f, 64 / 255f, 64 / 255f));
        label.sizing(Sizing.fixed(176), Sizing.content(2));
        label.horizontalTextAlignment(HorizontalAlignment.CENTER);
        label.zIndex(1);
        overlay.child(label.positioning(Positioning.relative(50, 2)));
    }
}
