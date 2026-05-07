package dev.overgrown.sync.power.type.entity_texture_overlay.client.render.feature;

import dev.overgrown.sync.power.type.entity_texture_overlay.EntityTextureOverlayPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
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
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PlayerTextureOverlayFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerTextureOverlayFeatureRenderer(
        FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {

        boolean isFirstPerson = MinecraftClient.getInstance().player == player &&
            MinecraftClient.getInstance().options.getPerspective().isFirstPerson();

        List<EntityTextureOverlayPowerType> powers =
            PowerHolderComponent.getPowerTypes(player, EntityTextureOverlayPowerType.class);
        if (powers.isEmpty()) return;

        EntityTextureOverlayPowerType power = powers.get(0);

        if (power.isActive() && power.shouldRenderAsOverlay() && (!isFirstPerson || power.shouldShowFirstPerson())) {
            boolean slim = player.getSkinTextures().model() == SkinTextures.Model.SLIM;
            Identifier texture = slim ? power.getSlimTextureLocation() : power.getWideTextureLocation();

            int color = ColorHelper.Argb.getArgb(
                (int)(power.getAlpha() * 255),
                (int)(power.getRed() * 255),
                (int)(power.getGreen() * 255),
                (int)(power.getBlue() * 255)
            );

            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
                RenderLayer.getEntityTranslucent(texture, false)
            );

            this.getContextModel().render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color);
        }
    }
}
