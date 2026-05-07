package dev.overgrown.sync.power.type.modify_player_model.client.render.feature;

import dev.overgrown.sync.power.type.entity_texture_overlay.EntityTextureOverlayPowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Stub feature renderer kept for parity with Sync 1.20.1. The body of this
 * renderer is intentionally empty in the original mod — extra limbs are drawn
 * by the FourArmsPlayerEntityModel itself, so this renderer only exists to
 * gate optional texture-overlay logic.
 */
@Environment(EnvType.CLIENT)
public class ExtraLimbsFeatureRenderer
    extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public ExtraLimbsFeatureRenderer(
        FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!PowerHolderComponent.getPowerTypes(player, EntityTextureOverlayPowerType.class).isEmpty()) {
            // Parity stub from 1.20.1; extra-limb visuals are produced by the model itself.
        }
    }
}
