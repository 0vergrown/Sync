package dev.overgrown.sync.power.type.energy_swirl.client.render.feature;

import dev.overgrown.sync.power.type.energy_swirl.EnergySwirlPowerType;
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
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EnergySwirlFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    private static final Identifier DEFAULT_TEXTURE = Identifier.ofVanilla("textures/entity/wither/wither_armor.png");

    public EnergySwirlFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {

        List<EnergySwirlPowerType> powers = PowerHolderComponent.getPowerTypes(entity, EnergySwirlPowerType.class);
        if (powers.isEmpty()) return;

        EnergySwirlPowerType power = powers.get(0);
        if (!power.isActive()) return;

        Identifier texture = power.getTextureLocation();
        if (texture == null) texture = DEFAULT_TEXTURE;

        float size = power.getSize();
        float speed = power.getSpeed();

        float xOffset;
        float yOffset;

        if (speed == 0.0f) {
            xOffset = 0.0f;
            yOffset = 0.0f;
        } else {
            float age = entity.age + tickDelta;
            xOffset = MathHelper.cos(age * 0.02f) * 3.0f;
            yOffset = age * speed % 1.0f;
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
            RenderLayer.getEnergySwirl(texture, xOffset % 1.0f, yOffset)
        );

        matrices.push();
        matrices.scale(size, size, size);

        EntityModel<T> model = this.getContextModel();
        model.animateModel(entity, limbAngle, limbDistance, tickDelta);
        model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);

        int color = ColorHelper.Argb.getArgb(255, 128, 128, 128);
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color);

        matrices.pop();
    }
}
