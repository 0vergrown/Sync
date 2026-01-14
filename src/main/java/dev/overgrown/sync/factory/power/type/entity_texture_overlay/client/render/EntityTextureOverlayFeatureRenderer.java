package dev.overgrown.sync.factory.power.type.entity_texture_overlay.client.render;

import dev.overgrown.sync.factory.power.type.entity_texture_overlay.EntityTextureOverlayPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class EntityTextureOverlayFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends FeatureRenderer<T, M> {

    public EntityTextureOverlayFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       T entity, float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {

        // Check if this is first person and we should skip
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective().isFirstPerson() &&
                entity == client.player) {
            // Don't render here - first person is handled separately
            return;
        }

        PowerHolderComponent.getPowers(entity, EntityTextureOverlayPower.class).forEach(power -> {
            if (power.isActive()) {
                renderOverlay(matrices, vertexConsumers, light, entity, power);
            }
        });
    }

    private void renderOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                               int light, T entity, EntityTextureOverlayPower power) {

        // Don't render if we shouldn't render the original model
        if (!power.shouldRenderOriginalModel()) {
            return;
        }

        Identifier texture = power.getTextureLocation();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityTranslucent(texture)
        );

        // Render the overlay using the entity's existing model
        this.getContextModel().render(matrices, vertexConsumer, light,
                OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}