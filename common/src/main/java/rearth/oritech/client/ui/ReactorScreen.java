package rearth.oritech.client.ui;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class ReactorScreen extends BaseOwoHandledScreen<FlowLayout, ReactorScreenHandler> {
    
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
        
        var overlay = Containers.horizontalFlow(Sizing.fixed(240), Sizing.fixed(200));
        
        if (handler.reactorEntity.uiData != null) {
            addReactorComponentPreview(overlay);
        }
        
        rootComponent.child(overlay.surface(Surface.PANEL));
        
        addTitle(overlay);
    }
    
    private void addReactorComponentPreview(FlowLayout overlay) {
        
        var holoPreviewContainer = Containers.horizontalFlow(Sizing.fixed(180), Sizing.fixed(180));
        holoPreviewContainer.surface(Surface.PANEL_INSET);
        holoPreviewContainer.margins(Insets.of(2));
        
        var uiData = handler.reactorEntity.uiData;
        var rightMostPoint = uiData.min();
        var leftMostPoint = uiData.previewMax();
        var dist = rightMostPoint.getManhattanDistance(leftMostPoint);
        
        var availableDist = 170 / 20;
        var scaling = availableDist / (float) dist * 2;
        if (dist > 10) scaling *= 1.15f;
        
        System.out.println(dist + " | " + scaling);
        
        BlockPos.stream(uiData.min(), uiData.previewMax()).forEach(pos -> {
            var offset = pos.subtract(uiData.min());
            var uiPos = new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
            var preview = Components.block(handler.world.getBlockState(pos), handler.world.getBlockEntity(pos))
                            .sizing(Sizing.fixed(170))
                            .positioning(Positioning.absolute(5, 85));
            if (uiPos.getY() == 1) {
                System.out.println("added tooltip!");
                preview.tooltip(handler.world.getBlockState(pos).getBlock().getName());
            }
            holoPreviewContainer.child(preview);
            
        });
        
        overlay.child(holoPreviewContainer);
        
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
