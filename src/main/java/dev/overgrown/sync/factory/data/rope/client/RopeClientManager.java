package dev.overgrown.sync.factory.data.rope.client;

import dev.overgrown.sync.factory.data.rope.common.RopePackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.overgrown.sync.factory.data.rope.common.RopeConstants.*;

public class RopeClientManager {

    // ropeId -> VerletRopeState. Mirrors the server's storage layout so a
    // single owner can have multiple ropes active at once.
    private static final Map<UUID, VerletRopeState> ropesById = new HashMap<>();
    private static final Map<UUID, Set<UUID>> ownerIndex = new HashMap<>();

    // ------ Rope Management Functions ------

    public static void create(UUID ropeId, UUID owner, Vec3d anchor, double length, float maxLength,
                              Identifier texture, int anchorEntityId, boolean leash) {
        VerletRopeState state = new VerletRopeState(ropeId, owner, anchor, length, maxLength, texture);
        state.anchorEntityId = anchorEntityId;
        state.leash = leash;
        ropesById.put(ropeId, state);
        ownerIndex.computeIfAbsent(owner, k -> new HashSet<>()).add(ropeId);
    }

    public static void delete(UUID ropeId) {
        VerletRopeState removed = ropesById.remove(ropeId);
        if (removed == null) return;
        Set<UUID> owned = ownerIndex.get(removed.owner);
        if (owned != null) {
            owned.remove(ropeId);
            if (owned.isEmpty()) ownerIndex.remove(removed.owner);
        }
    }

    public static void setTargetLength(UUID ropeId, double length) {
        VerletRopeState state = ropesById.get(ropeId);
        if (state != null) state.targetLength = length;
    }

    public static VerletRopeState get(UUID ropeId) {
        return ropesById.get(ropeId);
    }

    public static Collection<VerletRopeState> getAll() {
        return ropesById.values();
    }

    private static boolean ownerHasNonLeashRope(UUID owner) {
        Set<UUID> ids = ownerIndex.get(owner);
        if (ids == null) return false;
        for (UUID id : ids) {
            VerletRopeState s = ropesById.get(id);
            if (s != null && !s.leash) return true;
        }
        return false;
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

    // Reads jump/sneak input and sends a single length-change packet. The
    // server fans this out to every non-leash rope owned by the player, so we
    // only need to send it once regardless of rope count.
    private static void sendLocalLengthInput(MinecraftClient client) {
        double delta = 0;
        if (client.options.jumpKey.isPressed()) delta -= ROPE_LENGTH_CHANGE_STEP;
        if (client.options.sneakKey.isPressed()) delta += ROPE_LENGTH_CHANGE_STEP;
        if (delta != 0) sendChangeLength(delta);
    }

    // Per-rope visual interpolation between the current length and the last
    // targetLength broadcast from the server. Adjusts segment count with
    // hysteresis so the rope doesn't oscillate at segment boundaries.
    private static void interpolateRopeLength(VerletRopeState rope) {
        if (Math.abs(rope.targetLength - rope.length) <= 0.001) return;

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

        // Read any keybind inputs from client once per tick. Server fans out
        // the resulting packets to every non-leash rope the player owns.
        ClientPlayerEntity localPlayer = client.player;
        if (localPlayer != null && ownerHasNonLeashRope(localPlayer.getUuid())) {
            sendLocalLengthInput(client);
            if (!localPlayer.isOnGround()) swing(client);
        }

        // Update all ropes (physics + rendering)
        for (VerletRopeState rope : ropesById.values()) {
            PlayerEntity player = world.getPlayerByUuid(rope.owner);
            if (player == null) continue;

            // Keep anchor tracking its entity on the client too. verletStep
            // skips point 0, so if we don't manually roll prevPos forward, the
            // renderer lerps every frame's first segment between the *original*
            // attach position and the current anchor — which visually appears
            // as a stray rope trailing back to the spot where the target was
            // first grabbed.
            if (rope.anchorEntityId != VerletRopeState.NO_ANCHOR_ENTITY && !rope.points.isEmpty()) {
                Entity anchorEntity = world.getEntityById(rope.anchorEntityId);
                if (anchorEntity != null) {
                    RopePoint p0 = rope.points.get(0);
                    p0.prevPos = rope.anchor;
                    rope.anchor = anchorEntity.getBoundingBox().getCenter();
                    p0.pos = rope.anchor;
                }
            }

            // Per-rope visual length interpolation (driven by server-sent
            // targetLength). Skipped for leash ropes because the server never
            // updates their length.
            if (!rope.leash) interpolateRopeLength(rope);

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
        ropesById.clear();
        ownerIndex.clear();
    }
}
