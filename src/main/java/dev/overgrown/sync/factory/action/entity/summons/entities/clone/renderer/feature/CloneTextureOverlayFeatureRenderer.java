package dev.overgrown.sync.factory.action.entity.summons.entities.clone.renderer.feature;

import dev.overgrown.sync.factory.action.entity.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.factory.action.entity.summons.entities.clone.model.CloneEntityModel;
import dev.overgrown.sync.factory.power.type.entity_texture_overlay.EntityTextureOverlayPower;
import dev.overgrown.sync.factory.power.type.entity_texture_overlay.utils.RenderingUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Renders the owner's {@link EntityTextureOverlayPower} overlay on the clone,
 * so the clone visually matches the player that summoned it.
 * Only applies when the power is in overlay mode; texture-replacement mode is
 * handled in {@link dev.overgrown.sync.factory.action.entity.summons.entities.clone.renderer.CloneEntityRenderer#getTexture}.
 */
@Environment(EnvType.CLIENT)
public class CloneTextureOverlayFeatureRenderer<T extends CloneEntity>
        extends FeatureRenderer<T, CloneEntityModel<T>> {

    public CloneTextureOverlayFeatureRenderer(
            FeatureRendererContext<T, CloneEntityModel<T>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       T clone, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {

        if (!clone.isOwned()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        // Find the owner player in the current world
        PlayerEntity owner = client.world.getPlayerByUuid(clone.getOwnerUuid());
        if (owner == null) return;

        List<EntityTextureOverlayPower> powers = RenderingUtils.getTextureOverlays(owner);
        if (powers.isEmpty()) return;

        EntityTextureOverlayPower power = powers.get(0);
        // Only handle overlay mode here; replace mode is handled in getTexture()
        if (!power.isActive() || !power.shouldRenderAsOverlay()) return;

        // Determine slim/wide from the owner's skin model
        boolean slim = resolveSlim(client, clone);
        Identifier texture = slim ? power.getSlimTextureLocation() : power.getWideTextureLocation();

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityTranslucent(texture, false));

        this.getContextModel().render(
                matrices,
                vertexConsumer,
                light,
                OverlayTexture.DEFAULT_UV,
                power.getRed(),
                power.getGreen(),
                power.getBlue(),
                power.getAlpha()
        );
    }

    private boolean resolveSlim(MinecraftClient client, T clone) {
        if (client.getNetworkHandler() == null) return false;
        PlayerListEntry entry = client.getNetworkHandler()
                .getPlayerListEntry(clone.getOwnerUuid());
        return entry != null && entry.getModel().equals("slim");
    }
}