package dev.overgrown.sync.rope.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RopeRenderer {

    // How many world-units of rope one full V tile covers.
    // With a 8x16 texture and ROPE_WIDTH=0.1, one tile = 0.2 blocks looks clean.
    // Increase to stretch the texture out; decrease to tile it more tightly.
    private static final float V_TILE_LENGTH = 0.2f;

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(RopeRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        Map<UUID, VerletRopeState> ropes = RopeClientManager.getAll();
        if (ropes.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        float tickDelta = context.tickDelta();
        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();

        for (VerletRopeState state : ropes.values()) {
            List<RopePoint> points = state.points;
            if (points.size() < 2) continue;

            // getEntityCutoutNoCull samples the standalone texture as-is —
            // no font-atlas remapping like getText() does.
            RenderLayer layer = RenderLayer.getEntityCutoutNoCull(state.texture);
            layer.startDrawing();

            buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);

            Matrix4f mat = matrices.peek().getPositionMatrix();
            org.joml.Matrix3f normalMat = matrices.peek().getNormalMatrix();
            float halfWidth = dev.overgrown.sync.rope.common.RopeConstants.ROPE_WIDTH / 2f;
            int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
            int overlay = net.minecraft.client.render.OverlayTexture.DEFAULT_UV;

            // vOffset accumulates so the texture tiles continuously along the rope
            // rather than being stretched or reset per segment.
            float vOffset = 0f;

            for (int i = 0; i < points.size() - 1; i++) {
                RopePoint p0 = points.get(i);
                RopePoint p1 = points.get(i + 1);

                Vec3d a = p0.prevPos.lerp(p0.pos, tickDelta);
                Vec3d b = p1.prevPos.lerp(p1.pos, tickDelta);

                double segLen = a.distanceTo(b);
                float vNext = vOffset + (float) (segLen / V_TILE_LENGTH);

                Vec3d seg = b.subtract(a);
                double len = seg.length();
                if (len < 0.0001) { vOffset = vNext; continue; }
                Vec3d segN = seg.multiply(1.0 / len);

                // Billboard: side vector always faces the camera
                Vec3d mid = a.add(b).multiply(0.5);
                Vec3d toCamera = camPos.subtract(mid).normalize();
                Vec3d side = segN.crossProduct(toCamera).normalize().multiply(halfWidth);

                Vec3d a0 = a.subtract(side);
                Vec3d a1 = a.add(side);
                Vec3d b0 = b.subtract(side);
                Vec3d b1 = b.add(side);

                float nx = (float) segN.x, ny = (float) segN.y, nz = (float) segN.z;

                // u: 0->1 across the ribbon width (maps the full 8px of the texture)
                // v: tiles along the rope length  (maps the full 16px repeatedly)
                buf.vertex(mat, (float) a0.x, (float) a0.y, (float) a0.z)
                        .color(255, 255, 255, 255).texture(0f, vOffset)
                        .overlay(overlay).light(light).normal(normalMat, nx, ny, nz).next();
                buf.vertex(mat, (float) a1.x, (float) a1.y, (float) a1.z)
                        .color(255, 255, 255, 255).texture(1f, vOffset)
                        .overlay(overlay).light(light).normal(normalMat, nx, ny, nz).next();
                buf.vertex(mat, (float) b1.x, (float) b1.y, (float) b1.z)
                        .color(255, 255, 255, 255).texture(1f, vNext)
                        .overlay(overlay).light(light).normal(normalMat, nx, ny, nz).next();
                buf.vertex(mat, (float) b0.x, (float) b0.y, (float) b0.z)
                        .color(255, 255, 255, 255).texture(0f, vNext)
                        .overlay(overlay).light(light).normal(normalMat, nx, ny, nz).next();

                vOffset = vNext;
            }

            tessellator.draw();
            layer.endDrawing();
        }

        matrices.pop();
    }
}