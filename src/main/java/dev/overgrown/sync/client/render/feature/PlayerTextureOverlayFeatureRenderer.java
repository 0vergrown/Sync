package dev.overgrown.sync.client.render.feature;

import dev.overgrown.sync.utils.entity_texture_overlay.RenderingUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PlayerTextureOverlayFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerTextureOverlayFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {

        // Check if we should render in first person
        boolean isFirstPerson = MinecraftClient.getInstance().player == player &&
                MinecraftClient.getInstance().options.getPerspective().isFirstPerson();

        // Get the overlay power
        var powers = RenderingUtils.getTextureOverlays(player);
        if (!powers.isEmpty()) {
            var power = powers.get(0);

            // Check if power is active and should render as overlay
            if (power.isActive() && power.shouldRenderAsOverlay() && (!isFirstPerson || power.shouldShowFirstPerson())) {

                // Get the appropriate texture based on player model
                boolean slim = player.getModel().equals("slim");
                Identifier texture = slim ? power.getSlimTextureLocation() : power.getWideTextureLocation();

                // Get color from power
                float red = power.getRed();
                float green = power.getGreen();
                float blue = power.getBlue();
                float alpha = power.getAlpha();

                // For overlay mode, use translucent render layer
                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
                        RenderLayer.getEntityTranslucent(texture, false)
                );

                // Render the overlay on top of the existing skin
                this.getContextModel().render(
                        matrixStack,
                        vertexConsumer,
                        light,
                        OverlayTexture.DEFAULT_UV,
                        red, green, blue, alpha
                );
            }
        }
    }
}