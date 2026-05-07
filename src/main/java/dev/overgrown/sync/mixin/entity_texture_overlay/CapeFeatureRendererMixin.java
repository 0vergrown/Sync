package dev.overgrown.sync.mixin.entity_texture_overlay;

import dev.overgrown.sync.power.type.entity_texture_overlay.EntityTextureOverlayPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void sync$onRenderCape(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light,
                                   AbstractClientPlayerEntity player,
                                   float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        List<EntityTextureOverlayPowerType> powers =
            PowerHolderComponent.getPowerTypes(player, EntityTextureOverlayPowerType.class);
        for (EntityTextureOverlayPowerType power : powers) {
            if (power.isActive() && power.shouldHideCape()) {
                ci.cancel();
                return;
            }
        }
    }
}
