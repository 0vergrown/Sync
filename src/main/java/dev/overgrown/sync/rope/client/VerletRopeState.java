package dev.overgrown.sync.rope.client;

import dev.overgrown.sync.rope.common.RopeConstants;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VerletRopeState {

    public final UUID owner;
    public Vec3d anchor;
    public double length;
    public float maxLength;
    public Identifier texture;
    public double targetLength;
    public double segmentLength;
    public List<RopePoint> points = new ArrayList<>();

    public VerletRopeState(UUID owner, Vec3d anchor, double length, float maxLength, Identifier texture) {
        this.owner = owner;
        this.anchor = anchor;
        this.length = length;
        this.maxLength = maxLength;
        this.texture = texture;
        this.targetLength = length;
        this.segmentLength = RopeConstants.GOAL_ROPE_SEGMENT_LENGTH;
        initPoints(anchor, anchor); // will be updated on first tick
    }

    public void initPoints(Vec3d start, Vec3d end) {
        points.clear();
        int count = Math.max(2, (int) Math.ceil(length / segmentLength) + 1);
        for (int i = 0; i < count; i++) {
            double t = (double) i / (count - 1);
            Vec3d pos = start.lerp(end, t).add(0, -t * (1 - t) * length * 0.3, 0); // slight sag
            points.add(new RopePoint(pos));
        }
    }

    public void tick(Vec3d playerAttachPoint) {
        if (points.isEmpty()) return;

        // Update segment count to match current length
        int targetCount = Math.max(2, (int) Math.ceil(length / segmentLength) + 1);
        while (points.size() < targetCount) {
            points.add(new RopePoint(points.get(points.size() - 1).pos));
        }
        while (points.size() > targetCount) {
            points.remove(points.size() - 1);
        }

        int n = points.size();

        // Verlet integration
        for (int i = 1; i < n - 1; i++) {
            RopePoint p = points.get(i);
            Vec3d vel = p.pos.subtract(p.prevPos).multiply(RopeConstants.ROPE_DAMPING);
            p.prevPos = p.pos;
            p.pos = p.pos.add(vel).add(RopeConstants.GRAVITY);
        }

        // Constrain endpoints
        points.get(0).pos = anchor;
        points.get(0).prevPos = anchor;
        points.get(n - 1).pos = playerAttachPoint;
        points.get(n - 1).prevPos = playerAttachPoint;

        // Constraint relaxation passes
        for (int pass = 0; pass < 5; pass++) {
            // Anchor
            points.get(0).pos = anchor;
            points.get(n - 1).pos = playerAttachPoint;

            for (int i = 0; i < n - 1; i++) {
                RopePoint a = points.get(i);
                RopePoint b = points.get(i + 1);
                Vec3d diff = b.pos.subtract(a.pos);
                double dist = diff.length();
                if (dist < 0.0001) continue;
                double error = (dist - segmentLength) / dist * 0.5 * RopeConstants.ROPE_STIFFNESS;
                Vec3d correction = diff.multiply(error);
                if (i != 0) a.pos = a.pos.add(correction);
                if (i + 1 != n - 1) b.pos = b.pos.subtract(correction);
            }
        }

        // Smooth length transitions
        if (Math.abs(length - targetLength) > 0.01) {
            length += (targetLength - length) * 0.1;
        }
    }
}