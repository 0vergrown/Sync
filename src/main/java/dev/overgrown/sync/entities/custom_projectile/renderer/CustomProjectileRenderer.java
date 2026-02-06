package dev.overgrown.sync.entities.custom_projectile.renderer;

import dev.overgrown.sync.entities.custom_projectile.CustomProjectileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class CustomProjectileRenderer extends EntityRenderer<CustomProjectileEntity> {
    private static final Identifier MISSING = new Identifier("textures/misc/unknown_pack.png");

    public CustomProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(CustomProjectileEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(1.0F, 1.0F, 1.0F);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f posMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture(entity)));
        vertex(consumer, posMatrix, normalMatrix, light, 0.0F, 0, 0, 1);
        vertex(consumer, posMatrix, normalMatrix, light, 1.0F, 0, 1, 1);
        vertex(consumer, posMatrix, normalMatrix, light, 1.0F, 1, 1, 0);
        vertex(consumer, posMatrix, normalMatrix, light, 0.0F, 1, 0, 0);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(CustomProjectileEntity entity) {
        Identifier tex = entity.getTextureId();
        return tex != null ? tex : MISSING;
    }

    private static void vertex(VertexConsumer consumer, Matrix4f posMatrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {
        consumer.vertex(posMatrix, x - 0.5F, (float) y - 0.25F, 0.0F)
                .color(255, 255, 255, 255)
                .texture((float) u, (float) v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
                .next();
    }
}
