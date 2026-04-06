package dev.overgrown.sync.rope.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static dev.overgrown.sync.rope.common.RopeConstants.*;

public class RopeRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(RopeRenderer::render);
    }

    public static void render(WorldRenderContext context) {
        for (VerletRopeState rope : RopeClientManager.getAll()) {
            if (rope.points.size() < 2) continue;
            drawRope(context, rope);
        }
    }

    private static void drawRope(WorldRenderContext context, VerletRopeState rope) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        Camera camera = context.camera();

        if (matrices == null || consumers == null || camera == null) return;

        Vec3d cameraPos = camera.getPos();
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumer vc = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(rope.texture));

        float tickDelta = context.tickDelta();

        for (int i = 0; i < rope.points.size() - 1; i++) {
            RopePoint p0 = rope.points.get(i);
            RopePoint p1 = rope.points.get(i + 1);

            Vec3d A = p0.prevPos.lerp(p0.pos, tickDelta);
            Vec3d B = p1.prevPos.lerp(p1.pos, tickDelta);
            int light = WorldRenderer.getLightmapCoordinates(context.world(), BlockPos.ofFloored(A.add(B).multiply(0.5)));

            Vec3d dir = B.subtract(A);
            if (dir.lengthSquared() < 1e-6) continue;
            dir = dir.normalize();
            Vec3d view = cameraPos.subtract(A).normalize();
            Vec3d right = dir.crossProduct(view);
            if (right.lengthSquared() < 1e-6) continue;
            right = right.normalize().multiply(ROPE_WIDTH);

            Vec3d A1 = A.add(right);
            Vec3d A2 = A.subtract(right);
            Vec3d B1 = B.add(right);
            Vec3d B2 = B.subtract(right);

            vertex(matrices, vc, A1, 0f, 0f, light);
            vertex(matrices, vc, B1, 0f, 1f, light);
            vertex(matrices, vc, B2, 1f, 1f, light);
            vertex(matrices, vc, A2, 1f, 0f, light);
        }

        matrices.pop();
    }

    private static void vertex(MatrixStack matrices, VertexConsumer vc, Vec3d pos, float u, float v, int light) {
        MatrixStack.Entry entry = matrices.peek();
        vc.vertex(entry.getPositionMatrix(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry.getNormalMatrix(), 0, 1, 0)
                .next();
    }
}
