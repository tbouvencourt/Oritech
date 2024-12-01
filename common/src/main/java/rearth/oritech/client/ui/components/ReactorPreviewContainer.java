package rearth.oritech.client.ui.components;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;

import java.util.function.BiConsumer;

public class ReactorPreviewContainer extends FlowLayout {
    
    private final BiConsumer<Integer, Integer> onMouseMove;
    
    public ReactorPreviewContainer(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm, BiConsumer<Integer, Integer> onMouseMove) {
        super(horizontalSizing, verticalSizing, algorithm);
        this.onMouseMove = onMouseMove;
    }
    
    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        onMouseMove.accept(mouseX, mouseY);
    }
}
