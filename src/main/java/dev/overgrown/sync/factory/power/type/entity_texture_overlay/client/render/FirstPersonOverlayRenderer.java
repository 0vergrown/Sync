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
            PlayerEntityModel<AbstractClientPlayerEntity> model = playerRenderer.getModel();

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

        // Set up the model angles for first-person
        model.handSwingProgress = 0.0F;
        model.sneaking = false;
        model.leaningPitch = 0.0F;
        model.setAngles(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        matrices.push();

        // Adjust position for first-person view
        matrices.translate(0.0F, 0.1F, 0.0F);

        // Get vertex consumers for different parts
        VertexConsumer mainArmConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
        VertexConsumer offArmConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));

        // Render right arm (main hand for most players)
        model.rightArm.render(matrices, mainArmConsumer, light, OverlayTexture.DEFAULT_UV);
        model.rightSleeve.render(matrices, mainArmConsumer, light, OverlayTexture.DEFAULT_UV);

        // Render left arm (offhand)
        model.leftArm.render(matrices, offArmConsumer, light, OverlayTexture.DEFAULT_UV);
        model.leftSleeve.render(matrices, offArmConsumer, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}