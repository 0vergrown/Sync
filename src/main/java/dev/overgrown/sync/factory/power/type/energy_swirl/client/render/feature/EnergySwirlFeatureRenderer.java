package dev.overgrown.sync.factory.power.type.energy_swirl.client.render.feature;

import dev.overgrown.sync.factory.power.type.energy_swirl.EnergySwirlPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EnergySwirlFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public EnergySwirlFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {

        List<EnergySwirlPower> powers = PowerHolderComponent.getPowers(entity, EnergySwirlPower.class);
        if (powers.isEmpty()) {
            return;
        }

        EnergySwirlPower power = powers.get(0);
        if (!power.isActive()) {
            return;
        }

        Identifier texture = power.getTextureLocation();
        if (texture == null) {
            // Default to wither armor texture if none specified
            texture = new Identifier("textures/entity/wither/wither_armor.png");
        }

        float size = power.getSize();
        float speed = power.getSpeed();

        // Calculate animation offsets
        float xOffset;
        float yOffset;

        if (speed == 0.0f) {
            // Static overlay (no animation)
            xOffset = 0.0f;
            yOffset = 0.0f;
        } else {
            // Animated overlay (original behavior)
            float age = entity.age + tickDelta;
            xOffset = MathHelper.cos(age * 0.02f) * 3.0f; // Horizontal swirling motion
            yOffset = age * speed % 1.0f; // Vertical scrolling
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                RenderLayer.getEnergySwirl(texture, xOffset % 1.0f, yOffset)
        );

        // Apply size scaling
        matrices.push();
        matrices.scale(size, size, size);

        // Get and configure the model
        EntityModel<T> model = this.getContextModel();
        model.animateModel(entity, limbAngle, limbDistance, tickDelta);
        model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

        // Render the energy swirl
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV,
                0.5F, 0.5F, 0.5F, 1.0F);

        matrices.pop();
    }
}