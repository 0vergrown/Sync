package dev.overgrown.sync.mixin;

import dev.overgrown.sync.factory.power.type.entity_texture_overlay.client.render.FirstPersonOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class EntityTextureOverlayPlayerEntityRendererMixin {

    @Inject(method = "renderRightArm", at = @At("TAIL"))
    private void sync$renderFirstPersonOverlayOnRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                                         AbstractClientPlayerEntity player, CallbackInfo ci) {
        renderFirstPersonOverlay(player, matrices, vertexConsumers, light);
    }

    @Inject(method = "renderLeftArm", at = @At("TAIL"))
    private void sync$renderFirstPersonOverlayOnLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                                        AbstractClientPlayerEntity player, CallbackInfo ci) {
        renderFirstPersonOverlay(player, matrices, vertexConsumers, light);
    }

    @Unique
    private void renderFirstPersonOverlay(AbstractClientPlayerEntity player, MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers, int light) {
        // Only render in first person for the main player
        if (player == MinecraftClient.getInstance().player &&
                MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
            FirstPersonOverlayRenderer.renderFirstPersonOverlay(player, matrices, vertexConsumers, light);
        }
    }
}