package dev.overgrown.sync.factory.data.rope.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.overgrown.sync.factory.data.rope.common.RopeConstants.*;

public class VerletRopeState {

    public static final int NO_ANCHOR_ENTITY = -1;

    public final UUID ropeId;
    public final UUID owner;
    public Vec3d anchor;
    public int anchorEntityId = NO_ANCHOR_ENTITY;
    public boolean leash;
    public double length;
    public double targetLength;
    public float maxLength;
    public List<RopePoint> points;
    public double segmentLength;
    public Identifier texture;

    public VerletRopeState(UUID ropeId, UUID owner, Vec3d anchor, double length, float maxLength, Identifier texture) {
        this.ropeId = ropeId;
        this.owner = owner;
        this.anchor = anchor;
        this.length = length;
        this.targetLength = length;
        this.maxLength = maxLength;
        this.texture = texture;

        // Set number of rope points and the length between
        int segments = Math.max(2, (int) Math.round(length / GOAL_ROPE_SEGMENT_LENGTH));
        this.segmentLength = length / segments;
        this.points = new ArrayList<>(segments + 1);

        // Get player position for rope end
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        Vec3d playerPos = anchor.subtract(0, length, 0);

        // Get rope owner's position
        if (world != null) {
            PlayerEntity p = world.getPlayerByUuid(owner);
            if (p != null)
                playerPos = p.getBoundingBox().getCenter();
        }

        // Add some jitter to help rope collapse on itself (avoid singularities)
        Vec3d jitter = new Vec3d(
                (Math.random() - 0.5) * 1e-4,
                0,
                (Math.random() - 0.5) * 1e-4
        );

        // Initialize rope points linearly between anchor and player
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            Vec3d pos = anchor.lerp(playerPos, t);
            points.add(new RopePoint(pos.add(jitter)));
        }
    }
}
