package dev.overgrown.sync.factory.data.rope.client;

import dev.overgrown.sync.factory.data.rope.common.RopePackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.overgrown.sync.factory.data.rope.common.RopeConstants.*;

public class RopeClientManager {

    private static final Map<UUID, VerletRopeState> ropes = new HashMap<>();

    // ------ Rope Management Functions ------

    public static void create(UUID owner, Vec3d anchor, double length, float maxLength, Identifier texture) {
        VerletRopeState state = new VerletRopeState(owner, anchor, length, maxLength, texture);
        ropes.put(owner, state);
    }

    public static void delete(UUID owner) {
        ropes.remove(owner);
    }

    public static void setTargetLength(UUID owner, double length) {
        VerletRopeState state = ropes.get(owner);
        if (state != null) state.targetLength = length;
    }

    public static VerletRopeState get(UUID owner) {
        return ropes.get(owner);
    }

    public static Collection<VerletRopeState> getAll() {
        return ropes.values();
    }

    // ------ Rope Physics Functions ------

    private static void verletStep(VerletRopeState rope) {
        for (int i = 1; i < rope.points.size(); i++) {
            RopePoint p = rope.points.get(i);
            Vec3d vel = p.pos.subtract(p.prevPos).multiply(ROPE_DAMPING);
            p.prevPos = p.pos;
            p.pos = p.pos.add(vel).add(GRAVITY);
        }
    }

    private static void applyRopeConstraints(VerletRopeState rope, Vec3d playerPos) {
        // Pin both ends in place (anchor + player)
        rope.points.get(0).pos = rope.anchor;
        rope.points.get(rope.points.size() - 1).pos = playerPos;

        // Forwards pass of constraining
        for (int i = 0; i < rope.points.size() - 1; i++) {
            RopePoint a = rope.points.get(i);
            RopePoint b = rope.points.get(i + 1);

            Vec3d delta = b.pos.subtract(a.pos);
            double dist = delta.length();
            double diff = (dist - rope.segmentLength) / dist;

            Vec3d offset = delta.multiply(ROPE_STIFFNESS * diff);
            a.pos = a.pos.add(offset);
            b.pos = b.pos.subtract(offset);
        }

        // Repin both ends in place after tweaks
        rope.points.get(0).pos = rope.anchor;
        rope.points.get(rope.points.size() - 1).pos = playerPos;
    }

    // ------ Player Controls Functions ------

    private static void changeRopeLength(VerletRopeState rope, MinecraftClient client) {
        // Send a packet to server containing requested length change based on player inputs
        double delta = 0;
        if (client.options.jumpKey.isPressed()) delta -= ROPE_LENGTH_CHANGE_STEP;
        if (client.options.sneakKey.isPressed()) delta += ROPE_LENGTH_CHANGE_STEP;
        if (delta != 0) sendChangeLength(delta);

        // Apply length change if targetLength changed by server
        if (Math.abs(rope.targetLength - rope.length) > 0.001) {
            // Interpolate towards new length gradually
            rope.length = MathHelper.lerp(0.33, rope.length, rope.targetLength);

            // Snap when very close to avoid perpetual interpolation
            if (Math.abs(rope.targetLength - rope.length) < 0.01)
                rope.length = rope.targetLength;

            // Calculate how many segments rope SHOULD be at this length
            int currentSegments = rope.points.size() - 1;
            int desiredSegments = Math.max(2, (int) Math.ceil(rope.length / GOAL_ROPE_SEGMENT_LENGTH));
            int maxSegments = Math.max(2, Math.round(rope.maxLength / GOAL_ROPE_SEGMENT_LENGTH));
            desiredSegments = Math.min(desiredSegments, maxSegments);

            // Hysteresis: only add/remove segments when difference is >= 2
            // This prevents oscillation at segment count boundaries
            int diff = currentSegments - desiredSegments;

            if (diff >= 2) {
                // Too many, remove segment farthest from player
                rope.points.remove(1);
            } else if (diff <= -2) {
                // Too few, create a new one after anchor
                RopePoint last = rope.points.get(0);
                RopePoint prev = rope.points.get(1);
                Vec3d dir = last.pos.subtract(prev.pos);
                double dirLen = dir.length();
                if (dirLen > 1e-6) {
                    dir = dir.multiply(1.0 / dirLen);
                }
                Vec3d newPos = last.pos.add(dir.multiply(GOAL_ROPE_SEGMENT_LENGTH));

                RopePoint p = new RopePoint(newPos);

                // Set velocity of new point
                Vec3d vel = last.pos.subtract(last.prevPos);
                p.prevPos = newPos.subtract(vel);

                rope.points.add(1, p);
            }

            // Update ideal length between segments for smoother length changes
            rope.segmentLength = rope.length / (rope.points.size() - 1);
        }
    }

    private static void swing(MinecraftClient client) {
        GameOptions opts = client.options;

        double x = 0;
        double z = 0;

        if (opts.forwardKey.isPressed()) z += 1;
        if (opts.backKey.isPressed())    z -= 1;
        if (opts.rightKey.isPressed())   x += 1;
        if (opts.leftKey.isPressed())    x -= 1;
        if (x == 0 && z == 0) return;

        Vec3d dir = new Vec3d(x, 0, z);
        sendSwing(dir.lengthSquared() > 1 ? dir.normalize() : dir);
    }

    // ------ Tick Function ------

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return;

        // Read any keybind inputs from client (rope length change, swing inputs)
        ClientPlayerEntity localPlayer = client.player;
        if (localPlayer != null) {
            UUID localUuid = localPlayer.getUuid();
            VerletRopeState localRope = ropes.get(localUuid);
            if (localRope != null) {
                // Apply length changes via jump/sneak
                changeRopeLength(localRope, client);

                // Apply swing inputs via WASD whenever the player is off the ground.
                // The rope constraint handles the physics — no need to gate on
                // tautness or anchor height, which was preventing most swing input.
                if (!localPlayer.isOnGround())
                    swing(client);
            }
        }

        // Update all ropes (physics + rendering)
        for (VerletRopeState rope : ropes.values()) {
            PlayerEntity player = world.getPlayerByUuid(rope.owner);
            if (player == null) continue;
            Vec3d playerPos = player.getBoundingBox().getCenter();

            verletStep(rope);
            for (int i = 0; i < 10; i++)
                applyRopeConstraints(rope, playerPos);
        }
    }

    private static void sendChangeLength(double delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(client.player.getUuid());
        buf.writeDouble(delta);
        ClientPlayNetworking.send(RopePackets.ROPE_CHANGE_LENGTH, buf);
    }

    private static void sendSwing(Vec3d dir) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(dir.x);
        buf.writeDouble(dir.y);
        buf.writeDouble(dir.z);
        ClientPlayNetworking.send(RopePackets.ROPE_SWING, buf);
    }

    public static void clear() {
        ropes.clear();
    }
}
