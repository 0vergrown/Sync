package dev.overgrown.sync.rope.common;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.overgrown.sync.rope.common.RopeConstants.*;

public class RopeManager {

    private static final Map<UUID, RopeState> ropes = new HashMap<>();

    // ------ Rope Management Functions ------

    public static void attach(ServerPlayerEntity player, Vec3d anchor, float maxLength, Identifier texture) {
        UUID uuid = player.getUuid();
        double length = player.getBoundingBox().getCenter().distanceTo(anchor);
        length = Math.min(length, maxLength);
        RopeState state = new RopeState(anchor, uuid, length, maxLength, texture);
        ropes.put(uuid, state);

        // If player is flying, shorten rope to "pull" forwards, boosting elytra flight
        if (player.isFallFlying()) {
            if (player.isSneaking()) {
                player.stopFallFlying();
            } else {
                state.length = MathHelper.clamp(state.length - ELYTRA_LENGTH_MOD, MIN_ROPE_LENGTH, maxLength);
            }
        }

        // Broadcast ROPE_CREATE to all players
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        buf.writeDouble(anchor.x);
        buf.writeDouble(anchor.y);
        buf.writeDouble(anchor.z);
        buf.writeDouble(state.length);
        buf.writeFloat(maxLength);
        buf.writeIdentifier(texture);
        for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, RopePackets.ROPE_CREATE, buf);
        }
    }

    public static void detach(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        if (ropes.remove(uuid) != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(uuid);
            for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(p, RopePackets.ROPE_DELETE, buf);
            }
        }
    }

    public static RopeState get(UUID uuid) {
        return ropes.get(uuid);
    }

    public static boolean has(UUID uuid) {
        return ropes.containsKey(uuid);
    }

    // ------ Player Physics Functions ------

    private static void applyLeashConstraint(ServerPlayerEntity player, RopeState rope) {
        Vec3d anchor = rope.anchor;
        Vec3d pos = player.getBoundingBox().getCenter();
        Vec3d delta = pos.subtract(anchor);
        double dist = delta.length();

        // Rope not taut, no need for physics
        if (dist <= rope.length) return;

        // Reduce fall damage if currently supported by rope from above
        if (rope.anchor.y > player.getY())
            player.fallDistance = Math.max(0, player.fallDistance - 1.0f);

        // Calculate direction
        Vec3d dir = delta.normalize();
        double excess = dist - rope.length;

        // Split velocity into radial (out from rope) and tangential (perpendicular)
        Vec3d vel = player.getVelocity();
        double radial = vel.dotProduct(dir);

        Vec3d radialVel = dir.multiply(radial);
        Vec3d tangentialVel = vel.subtract(radialVel);

        // Damp radial (outwards) velocity
        if (radial > 0)
            radialVel = radialVel.multiply(RADIAL_DAMPING);

        // Inject tangential velocity for more powerful swings while player sprints
        if (player.isSprinting() && tangentialVel.length() < MAX_SWING_SPEED)
            tangentialVel = tangentialVel.normalize().multiply(tangentialVel.length() * SWING_BOOST);

        player.setVelocity(tangentialVel.add(radialVel));

        // Spring, slightly reduced on return only
        double springScale = radial < 0 ? SPRING_SCALING : 1.0;
        Vec3d correction = dir.multiply(-excess * LEASH_STIFFNESS * springScale);
        player.addVelocity(correction.x, correction.y, correction.z);

        player.velocityModified = true;
    }

    // ------ Player Controls Functions ------

    public static void handleChangeLength(ServerPlayerEntity player, double delta) {
        RopeState state = ropes.get(player.getUuid());
        if (state == null) return;

        // If rope is slack, increase pull rate
        double dist = player.getBoundingBox().getCenter().subtract(state.anchor).length();
        if (delta < 0 && dist < 0.95 * state.length)
            delta *= SLACK_PULL_RATE_MULT;

        // Apply requested change (w/ rope length limits)
        state.length = MathHelper.clamp(state.length + delta, MIN_ROPE_LENGTH, state.maxLength);

        // Send update to client
        sendVerletLength(player);
    }

    public static void handleSwing(ServerPlayerEntity player, Vec3d inputDir) {
        RopeState state = ropes.get(player.getUuid());
        if (state == null) return;

        // Use only the horizontal component of the player's look direction
        // so looking up at the anchor doesn't pull the player toward it
        double yawRad = Math.toRadians(player.getYaw());
        Vec3d forward = new Vec3d(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3d right = new Vec3d(forward.z, 0, -forward.x);

        // Combine WASD input into horizontal velocity
        Vec3d localMomentum = forward.multiply(inputDir.z).add(right.multiply(inputDir.x));

        double swingForce = 0.02;
        player.addVelocity(localMomentum.x * swingForce, 0, localMomentum.z * swingForce);
        player.velocityModified = true;
    }

    // ------ Tick Function ------

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            RopeState rope = ropes.get(player.getUuid());
            if (rope == null) continue;

            // Handle elytra restrictions: detach after time limit
            if (player.isFallFlying()) {
                rope.playerFlightTicks += 1;
                if (rope.playerFlightTicks >= ELYTRA_TIME_LIMIT)
                    detach(player);
            } else {
                rope.playerFlightTicks = 0;
            }

            // Apply player physics, separate from Verlet rope visual
            applyLeashConstraint(player, rope);
        }
    }

    private static void sendVerletLength(ServerPlayerEntity player) {
        RopeState state = ropes.get(player.getUuid());
        if (state == null) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(player.getUuid());
        buf.writeDouble(state.length);
        for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, RopePackets.ROPE_VERLET_LENGTH, buf);
        }
    }

    public static void removeAll() {
        ropes.clear();
    }
}
