package dev.overgrown.sync.factory.power.type.entity_texture_overlay.client.render;

import dev.overgrown.sync.factory.power.type.entity_texture_overlay.EntityTextureOverlayPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class FirstPersonOverlayRenderer {

    public static void renderFirstPersonOverlay(AbstractClientPlayerEntity player,
                                                MatrixStack matrices,
                                                VertexConsumerProvider vertexConsumers,
                                                int light) {

        // Check if we should render any overlays
        boolean shouldRender = PowerHolderComponent.getPowers(player, EntityTextureOverlayPower.class)
                .stream()
                .anyMatch(power -> power.isActive() && power.shouldShowFirstPerson());

        if (!shouldRender) return;

        // Get the player model from the player entity renderer
        if (MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof net.minecraft.client.render.entity.PlayerEntityRenderer playerRenderer) {
            PlayerEntityModel<AbstractClientPlayerEntity> model = (PlayerEntityModel<AbstractClientPlayerEntity>) playerRenderer.getModel();

            // Render each active overlay power
            PowerHolderComponent.getPowers(player, EntityTextureOverlayPower.class).forEach(power -> {
                if (power.isActive() && power.shouldShowFirstPerson()) {
                    renderFirstPersonOverlayForPower(matrices, vertexConsumers, light, player, model, power);
                }
            });
        }
    }

    private static void renderFirstPersonOverlayForPower(MatrixStack matrices,
                                                         VertexConsumerProvider vertexConsumers,
                                                         int light,
                                                         AbstractClientPlayerEntity player,
                                                         PlayerEntityModel<AbstractClientPlayerEntity> model,
                                                         EntityTextureOverlayPower power) {

        Identifier texture = power.getTextureLocation();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityTranslucent(texture)
        );

        // Set up model angles for first-person rendering
        model.handSwingProgress = 0.0F;
        model.sneaking = false;
        model.leaningPitch = 0.0F;
        model.setAngles(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        // Render only the arms for first-person view
        matrices.push();

        // Adjust position for first-person view
        matrices.translate(0.0F, 0.0F, 0.0F);

        // Render the model parts that should be visible in first person
        model.render(matrices, vertexConsumer, light,
                OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
    }
}