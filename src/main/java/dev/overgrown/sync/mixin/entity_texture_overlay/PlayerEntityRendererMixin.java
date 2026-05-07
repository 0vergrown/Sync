package dev.overgrown.sync.mixin.entity_texture_overlay;

import dev.overgrown.sync.power.type.entity_texture_overlay.EntityTextureOverlayPowerType;
import dev.overgrown.sync.power.type.entity_texture_overlay.client.render.feature.PlayerTextureOverlayFeatureRenderer;
import io.github.apace100.apoli.component.PowerHolderComponent;
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
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sync$addCustomFeature(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        this.addFeature(new PlayerTextureOverlayFeatureRenderer(this));
    }

    @Inject(method = "renderRightArm", at = @At("TAIL"))
    private void sync$renderRightArmOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                            int light, AbstractClientPlayerEntity player, CallbackInfo ci) {
        sync$renderArmOverlay(matrices, vertexConsumers, light, player, true);
    }

    @Inject(method = "renderLeftArm", at = @At("TAIL"))
    private void sync$renderLeftArmOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                           int light, AbstractClientPlayerEntity player, CallbackInfo ci) {
        sync$renderArmOverlay(matrices, vertexConsumers, light, player, false);
    }

    @Unique
    private void sync$renderArmOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                       int light, AbstractClientPlayerEntity player, boolean isRightArm) {
        if (MinecraftClient.getInstance().player == player
            && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {

            List<EntityTextureOverlayPowerType> powers =
                PowerHolderComponent.getPowerTypes(player, EntityTextureOverlayPowerType.class);
            if (powers.isEmpty()) return;

            EntityTextureOverlayPowerType power = powers.get(0);

            if (power.isActive() && power.shouldRenderAsOverlay() && power.shouldShowFirstPerson()) {
                boolean slim = player.getSkinTextures().model() == SkinTextures.Model.SLIM;
                Identifier texture = slim ? power.getSlimTextureLocation() : power.getWideTextureLocation();

                int color = ColorHelper.Argb.getArgb(
                    (int)(power.getAlpha() * 255),
                    (int)(power.getRed() * 255),
                    (int)(power.getGreen() * 255),
                    (int)(power.getBlue() * 255)
                );

                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture, false));

                if (isRightArm) {
                    this.getModel().rightArm.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color);
                    this.getModel().rightSleeve.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color);
                } else {
                    this.getModel().leftArm.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color);
                    this.getModel().leftSleeve.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color);
                }
            }
        }
    }
}
