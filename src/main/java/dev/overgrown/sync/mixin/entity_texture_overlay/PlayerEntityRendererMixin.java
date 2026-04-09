package dev.overgrown.sync.mixin.entity_texture_overlay;

import dev.overgrown.sync.factory.power.type.entity_texture_overlay.client.render.feature.PlayerTextureOverlayFeatureRenderer;
import dev.overgrown.sync.factory.power.type.entity_texture_overlay.utils.RenderingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(
            method = "<init>",
            at = @At(
                    "TAIL"
            )
    )
    private void addCustomFeature(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        this.addFeature(new PlayerTextureOverlayFeatureRenderer(this));
    }

    @Inject(
            method = "renderRightArm",
            at = @At(
                    "TAIL"
            )
    )
    private void renderRightArmOverlay(net.minecraft.client.util.math.MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo ci) {
        renderArmOverlay(matrices, vertexConsumers, light, player, true);
    }

    @Inject(
            method = "renderLeftArm",
            at = @At(
                    "TAIL"
            )
    )
    private void renderLeftArmOverlay(net.minecraft.client.util.math.MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo ci) {
        renderArmOverlay(matrices, vertexConsumers, light, player, false);
    }

    @Unique
    private void renderArmOverlay(net.minecraft.client.util.math.MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, boolean isRightArm) {
        // Check if we're in first person
        if (MinecraftClient.getInstance().player == player && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
            var powers = RenderingUtils.getTextureOverlays(player);
            if (!powers.isEmpty()) {
                var power = powers.get(0);

                // Only render overlay in first person if configured to do so
                if (power.isActive() && power.shouldRenderAsOverlay() && power.shouldShowFirstPerson()) {
                    boolean slim = player.getModel().equals("slim");
                    Identifier texture = slim ? power.getSlimTextureLocation() : power.getWideTextureLocation();

                    // Get color from power
                    float red = power.getRed();
                    float green = power.getGreen();
                    float blue = power.getBlue();
                    float alpha = power.getAlpha();

                    // Render overlay on the arm (after the base skin has been rendered)
                    VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture, false));

                    if (isRightArm) {
                        this.getModel().rightArm.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, red, green, blue, alpha);
                        this.getModel().rightSleeve.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, red, green, blue, alpha);
                    } else {
                        this.getModel().leftArm.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, red, green, blue, alpha);
                        this.getModel().leftSleeve.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, red, green, blue, alpha);
                    }
                }
            }
        }
    }
}