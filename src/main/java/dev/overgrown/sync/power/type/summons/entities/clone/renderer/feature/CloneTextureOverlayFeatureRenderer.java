package dev.overgrown.sync.power.type.summons.entities.clone.renderer.feature;

import dev.overgrown.sync.power.type.entity_texture_overlay.EntityTextureOverlayPowerType;
import dev.overgrown.sync.power.type.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.power.type.summons.entities.clone.model.CloneEntityModel;
import io.github.apace100.apoli.component.PowerHolderComponent;
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
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.List;

/**
 * Renders the owner's {@link EntityTextureOverlayPowerType} overlay on the clone
 * so the clone visually matches the player that summoned it.
 */
@Environment(EnvType.CLIENT)
public class CloneTextureOverlayFeatureRenderer<T extends CloneEntity>
    extends FeatureRenderer<T, CloneEntityModel<T>> {

    public CloneTextureOverlayFeatureRenderer(FeatureRendererContext<T, CloneEntityModel<T>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       T clone, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {

        if (!clone.isOwned()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        PlayerEntity owner = client.world.getPlayerByUuid(clone.getOwnerUuid());
        if (owner == null) return;

        List<EntityTextureOverlayPowerType> powers =
            PowerHolderComponent.getPowerTypes(owner, EntityTextureOverlayPowerType.class);
        if (powers.isEmpty()) return;

        EntityTextureOverlayPowerType power = powers.get(0);
        if (!power.isActive() || !power.shouldRenderAsOverlay()) return;

        boolean slim = sync$resolveSlim(client, clone);
        Identifier texture = slim ? power.getSlimTextureLocation() : power.getWideTextureLocation();

        int color = ColorHelper.Argb.getArgb(
            (int) (power.getAlpha() * 255),
            (int) (power.getRed() * 255),
            (int) (power.getGreen() * 255),
            (int) (power.getBlue() * 255)
        );

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
            RenderLayer.getEntityTranslucent(texture, false));

        this.getContextModel().render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color);
    }

    private boolean sync$resolveSlim(MinecraftClient client, T clone) {
        if (client.getNetworkHandler() == null) return false;
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(clone.getOwnerUuid());
        return entry != null && entry.getSkinTextures().model() == SkinTextures.Model.SLIM;
    }
}
