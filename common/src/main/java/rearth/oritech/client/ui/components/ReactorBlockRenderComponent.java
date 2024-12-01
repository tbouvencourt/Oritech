package rearth.oritech.client.ui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ReactorBlockRenderComponent extends BaseComponent {
    
    private final MinecraftClient client = MinecraftClient.getInstance();
    
    public final BlockState state;
    private final @Nullable BlockEntity entity;
    private final int zIndex;
    public final BlockPos pos;
    
    public ReactorBlockRenderComponent(BlockState state, @Nullable BlockEntity entity, int zIndex, BlockPos pos) {
        this.state = state;
        this.entity = entity;
        this.zIndex = zIndex;
        this.pos = pos;
    }
    
    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        context.getMatrices().push();
        
        context.getMatrices().translate(x + this.width / 2f, y + this.height / 2f, zIndex * 25 + 1000);
        context.getMatrices().scale(40 * this.width / 64f, -40 * this.height / 64f, 40);
        
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45 + 180));
        
        context.getMatrices().translate(-.5, -.5, -.5);
        
        RenderSystem.runAsFancy(() -> {
            final var vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
            if (this.state.getRenderType() != BlockRenderType.ENTITYBLOCK_ANIMATED) {
                this.client.getBlockRenderManager().renderBlockAsEntity(
                  this.state, context.getMatrices(), vertexConsumers,
                  LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV
                );
            }
            
            if (this.entity != null) {
                var renderer = this.client.getBlockEntityRenderDispatcher().get(this.entity);
                if (renderer != null) {
                    renderer.render(entity, partialTicks, context.getMatrices(), vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
                }
            }
            
            RenderSystem.setShaderLights(new Vector3f(-1.5f, -.5f, 0), new Vector3f(0, -1, 0));
            vertexConsumers.draw();
            DiffuseLighting.enableGuiDepthLighting();
        });
        
        context.getMatrices().pop();
    }
}
