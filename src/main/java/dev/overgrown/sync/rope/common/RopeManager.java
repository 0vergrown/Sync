package dev.overgrown.sync.rope.common;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RopeManager {

    private static final Map<UUID, RopeState> ropes = new HashMap<>();

    public static void attach(ServerPlayerEntity player, Vec3d anchor, float maxLength, Identifier texture) {
        UUID uuid = player.getUuid();
        double length = player.getPos().distanceTo(anchor);
        length = Math.min(length, maxLength);
        RopeState state = new RopeState(anchor, uuid, length, maxLength, texture);
        ropes.put(uuid, state);

        // Broadcast ROPE_CREATE to all players
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        buf.writeDouble(anchor.x);
        buf.writeDouble(anchor.y);
        buf.writeDouble(anchor.z);
        buf.writeDouble(length);
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

    public static void tick(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        RopeState state = ropes.get(uuid);
        if (state == null) return;

        Vec3d playerPos = player.getPos().add(0, player.getEyeHeight(player.getPose()) * 0.9, 0);
        Vec3d anchor = state.anchor;
        Vec3d delta = playerPos.subtract(anchor);
        double dist = delta.length();

        if (dist < 0.001) return;

        Vec3d dir = delta.normalize();

        // Radial damping — cancel outward velocity when past rope length
        if (dist > state.length) {
            Vec3d vel = player.getVelocity();
            double radialVel = vel.dotProduct(dir);
            if (radialVel > 0) {
                Vec3d damped = vel.subtract(dir.multiply(radialVel * RopeConstants.RADIAL_DAMPING));
                player.setVelocity(damped);
            }

            // Spring pull back
            double excess = dist - state.length;
            double pullRate = (state.slack > 0) ? RopeConstants.LEASH_STIFFNESS * RopeConstants.SLACK_PULL_RATE_MULT : RopeConstants.LEASH_STIFFNESS;
            Vec3d spring = dir.multiply(-excess * pullRate * RopeConstants.SPRING_SCALING);
            player.addVelocity(spring.x, spring.y, spring.z);
        }

        // Swing boost — boost horizontal velocity perpendicular to rope
        Vec3d vel = player.getVelocity();
        Vec3d horizontalDir = new Vec3d(dir.x, 0, dir.z).normalize();
        Vec3d perp = new Vec3d(-horizontalDir.z, 0, horizontalDir.x);
        double swingComponent = vel.dotProduct(perp);
        if (Math.abs(swingComponent) < RopeConstants.MAX_SWING_SPEED) {
            player.addVelocity(
                    perp.x * swingComponent * (RopeConstants.SWING_BOOST - 1),
                    0,
                    perp.z * swingComponent * (RopeConstants.SWING_BOOST - 1)
            );
        }

        // Elytra boost: allow brief glide while attached
        if (player.isFallFlying()) {
            state.playerFlightTicks++;
            if (state.playerFlightTicks > RopeConstants.ELYTRA_TIME_LIMIT) {
                state.length = Math.max(state.length - RopeConstants.ELYTRA_LENGTH_MOD, RopeConstants.MIN_ROPE_LENGTH);
                sendVerletLength(player);
            }
        } else {
            state.playerFlightTicks = 0;
        }

        state.slack = Math.max(0, state.length - dist);
    }

    public static void handleChangeLength(ServerPlayerEntity player, double delta) {
        RopeState state = ropes.get(player.getUuid());
        if (state == null) return;
        state.length = Math.max(RopeConstants.MIN_ROPE_LENGTH, Math.min(state.maxLength, state.length + delta));
        sendVerletLength(player);
    }

    public static void handleSwing(ServerPlayerEntity player, Vec3d inputDir) {
        RopeState state = ropes.get(player.getUuid());
        if (state == null || inputDir.lengthSquared() < 0.001) return;
        Vec3d norm = inputDir.normalize();
        player.addVelocity(norm.x * 0.1, 0, norm.z * 0.1);
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