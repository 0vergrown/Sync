package dev.overgrown.sync.mixin.entity_texture_overlay;

import dev.overgrown.sync.utils.entity_texture_overlay.RenderingUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderCape(net.minecraft.client.util.math.MatrixStack matrixStack,
                              net.minecraft.client.render.VertexConsumerProvider vertexConsumerProvider,
                              int i,
                              AbstractClientPlayerEntity abstractClientPlayerEntity,
                              float f, float g, float h, float j, float k, float l,
                              CallbackInfo ci) {
        // Check if the player has a texture overlay power that hides the cape
        if (RenderingUtils.shouldHideCape(abstractClientPlayerEntity)) {
            ci.cancel(); // Cancel cape rendering
        }
    }
}