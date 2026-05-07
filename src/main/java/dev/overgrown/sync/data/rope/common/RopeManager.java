package dev.overgrown.sync.data.rope.common;

import dev.overgrown.sync.data.rope.payload.s2c.RopeCreatePayload;
import dev.overgrown.sync.data.rope.payload.s2c.RopeDeletePayload;
import dev.overgrown.sync.data.rope.payload.s2c.RopeVerletLengthPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.overgrown.sync.data.rope.common.RopeConstants.*;

public class RopeManager {

    private static final Map<UUID, RopeState> ropesById = new HashMap<>();
    private static final Map<UUID, Set<UUID>> ownerIndex = new HashMap<>();

    private static MinecraftServer cachedServer;
    private static final double ANCHOR_MATCH_TOLERANCE = 0.6;

    public static UUID attach(ServerPlayerEntity player, Vec3d anchor, float maxLength, Identifier texture) {
        return attachInternal(player, anchor, RopeState.NO_ANCHOR_ENTITY, false, maxLength, texture);
    }

    public static UUID attachToEntity(ServerPlayerEntity player, Entity anchorEntity, float maxLength, Identifier texture) {
        return attachInternal(player, anchorEntity.getBoundingBox().getCenter(), anchorEntity.getId(), false, maxLength, texture);
    }

    public static UUID attachAsLeash(ServerPlayerEntity player, Entity anchorEntity, float maxLength, Identifier texture) {
        return attachInternal(player, anchorEntity.getBoundingBox().getCenter(), anchorEntity.getId(), true, maxLength, texture);
    }

    private static UUID attachInternal(ServerPlayerEntity player, Vec3d anchor, int anchorEntityId,
                                       boolean leash, float maxLength, Identifier texture) {
        UUID ownerUuid = player.getUuid();
        UUID ropeId = UUID.randomUUID();

        double length = player.getBoundingBox().getCenter().distanceTo(anchor);
        length = Math.min(length, maxLength);
        RopeState state = new RopeState(ropeId, anchor, ownerUuid, length, maxLength, texture);
        state.anchorEntityId = anchorEntityId;
        state.leash = leash;

        ropesById.put(ropeId, state);
        ownerIndex.computeIfAbsent(ownerUuid, k -> new HashSet<>()).add(ropeId);

        if (!leash && player.isFallFlying()) {
            if (player.isSneaking()) {
                player.stopFallFlying();
            } else {
                state.length = MathHelper.clamp(state.length - ELYTRA_LENGTH_MOD, MIN_ROPE_LENGTH, maxLength);
            }
        }

        broadcastCreate(player, state);
        return ropeId;
    }

    private static void broadcastCreate(ServerPlayerEntity player, RopeState state) {
        RopeCreatePayload payload = new RopeCreatePayload(
            state.ropeId, state.owner, state.anchor, state.length, state.maxLength,
            state.texture, state.anchorEntityId, state.leash
        );
        for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    private static void broadcastDelete(MinecraftServer server, UUID ropeId) {
        if (server == null) return;
        RopeDeletePayload payload = new RopeDeletePayload(ropeId);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    public static void detach(UUID ropeId) {
        RopeState removed = ropesById.remove(ropeId);
        if (removed == null) return;

        Set<UUID> ownerRopes = ownerIndex.get(removed.owner);
        if (ownerRopes != null) {
            ownerRopes.remove(ropeId);
            if (ownerRopes.isEmpty()) ownerIndex.remove(removed.owner);
        }

        broadcastDelete(cachedServer, ropeId);
    }

    public static void detachAll(UUID ownerUuid) {
        Set<UUID> ropeIds = ownerIndex.remove(ownerUuid);
        if (ropeIds == null || ropeIds.isEmpty()) return;
        for (UUID ropeId : ropeIds) {
            ropesById.remove(ropeId);
            broadcastDelete(cachedServer, ropeId);
        }
    }

    public static void detach(ServerPlayerEntity player) {
        detachAll(player.getUuid());
    }

    public static boolean detachByAnchorEntity(UUID ownerUuid, int anchorEntityId) {
        Set<UUID> ropeIds = ownerIndex.get(ownerUuid);
        if (ropeIds == null) return false;

        UUID matching = null;
        for (UUID ropeId : ropeIds) {
            RopeState state = ropesById.get(ropeId);
            if (state != null && state.anchorEntityId == anchorEntityId) {
                matching = ropeId;
                break;
            }
        }
        if (matching == null) return false;
        detach(matching);
        return true;
    }

    public static boolean detachByAnchorPos(UUID ownerUuid, Vec3d anchorPos) {
        Set<UUID> ropeIds = ownerIndex.get(ownerUuid);
        if (ropeIds == null) return false;

        UUID matching = null;
        double matchSq = ANCHOR_MATCH_TOLERANCE * ANCHOR_MATCH_TOLERANCE;
        for (UUID ropeId : ropeIds) {
            RopeState state = ropesById.get(ropeId);
            if (state == null) continue;
            if (state.anchorEntityId != RopeState.NO_ANCHOR_ENTITY) continue;
            if (state.anchor.squaredDistanceTo(anchorPos) <= matchSq) {
                matching = ropeId;
                break;
            }
        }
        if (matching == null) return false;
        detach(matching);
        return true;
    }

    public static RopeState get(UUID ropeId) {
        return ropesById.get(ropeId);
    }

    public static boolean has(UUID ownerUuid) {
        Set<UUID> ropeIds = ownerIndex.get(ownerUuid);
        return ropeIds != null && !ropeIds.isEmpty();
    }

    public static int countRopesByOwner(UUID ownerUuid, RopeFilter filter) {
        Set<UUID> ropeIds = ownerIndex.get(ownerUuid);
        if (ropeIds == null || ropeIds.isEmpty()) return 0;
        int count = 0;
        for (UUID ropeId : ropeIds) {
            RopeState state = ropesById.get(ropeId);
            if (state != null && filter.matches(state)) count++;
        }
        return count;
    }

    public static List<RopeState> getByOwner(UUID ownerUuid) {
        Set<UUID> ropeIds = ownerIndex.get(ownerUuid);
        if (ropeIds == null || ropeIds.isEmpty()) return Collections.emptyList();
        List<RopeState> out = new ArrayList<>(ropeIds.size());
        for (UUID ropeId : ropeIds) {
            RopeState state = ropesById.get(ropeId);
            if (state != null) out.add(state);
        }
        return out;
    }

    private static void applyLeashConstraint(ServerPlayerEntity player, RopeState rope) {
        Vec3d anchor = rope.anchor;
        Vec3d pos = player.getBoundingBox().getCenter();
        Vec3d delta = pos.subtract(anchor);
        double dist = delta.length();
        if (dist <= rope.length) return;

        if (rope.anchor.y > player.getY())
            player.fallDistance = Math.max(0, player.fallDistance - 1.0f);

        Vec3d dir = delta.normalize();
        double excess = dist - rope.length;

        Vec3d vel = player.getVelocity();
        double radial = vel.dotProduct(dir);

        Vec3d radialVel = dir.multiply(radial);
        Vec3d tangentialVel = vel.subtract(radialVel);

        if (radial > 0) radialVel = radialVel.multiply(RADIAL_DAMPING);

        if (player.isSprinting() && tangentialVel.length() < MAX_SWING_SPEED)
            tangentialVel = tangentialVel.normalize().multiply(tangentialVel.length() * SWING_BOOST);

        player.setVelocity(tangentialVel.add(radialVel));

        double springScale = radial < 0 ? SPRING_SCALING : 1.0;
        Vec3d correction = dir.multiply(-excess * LEASH_STIFFNESS * springScale);
        player.addVelocity(correction.x, correction.y, correction.z);
        player.velocityModified = true;
    }

    private static void applyLeashPullOnTarget(ServerPlayerEntity player, RopeState rope, Entity target) {
        Vec3d playerCenter = player.getBoundingBox().getCenter();
        Vec3d targetCenter = target.getBoundingBox().getCenter();
        Vec3d delta = playerCenter.subtract(targetCenter);
        double dist = delta.length();
        if (dist <= rope.length) return;

        Vec3d dir = delta.normalize();
        double excess = dist - rope.length;
        Vec3d correction = dir.multiply(excess * LEASH_STIFFNESS);

        Vec3d vel = target.getVelocity();
        double awayComponent = vel.dotProduct(dir.multiply(-1));
        if (awayComponent > 0) {
            Vec3d awayVel = dir.multiply(-awayComponent);
            Vec3d tangentialVel = vel.subtract(awayVel);
            awayVel = awayVel.multiply(RADIAL_DAMPING);
            target.setVelocity(tangentialVel.add(awayVel));
        }

        target.addVelocity(correction.x, correction.y, correction.z);
        target.velocityModified = true;

        if (target instanceof LivingEntity living) {
            living.fallDistance = Math.max(0, living.fallDistance - 1.0f);
        }
    }

    public static void handleChangeLength(ServerPlayerEntity player, double delta) {
        List<RopeState> owned = getByOwner(player.getUuid());
        if (owned.isEmpty()) return;

        for (RopeState state : owned) {
            if (state.leash) continue;
            double effectiveDelta = delta;
            double dist = player.getBoundingBox().getCenter().subtract(state.anchor).length();
            if (effectiveDelta < 0 && dist < 0.95 * state.length)
                effectiveDelta *= SLACK_PULL_RATE_MULT;
            double newLen = MathHelper.clamp(state.length + effectiveDelta, MIN_ROPE_LENGTH, state.maxLength);
            if (newLen != state.length) {
                state.length = newLen;
                sendVerletLength(player.getServer(), state);
            }
        }
    }

    public static void handleSwing(ServerPlayerEntity player, Vec3d inputDir) {
        List<RopeState> owned = getByOwner(player.getUuid());
        if (owned.isEmpty()) return;

        boolean anySwingRope = false;
        for (RopeState state : owned) {
            if (!state.leash) { anySwingRope = true; break; }
        }
        if (!anySwingRope) return;

        Vec3d forward = Vec3d.fromPolar(player.getPitch(), player.getYaw());
        Vec3d right = forward.crossProduct(new Vec3d(0, 1, 0));
        if (right.lengthSquared() < 1e-6) {
            double yawRad = Math.toRadians(player.getYaw());
            right = new Vec3d(Math.cos(yawRad), 0, Math.sin(yawRad));
        } else {
            right = right.normalize();
        }

        Vec3d localMomentum = forward.multiply(inputDir.z).add(right.multiply(inputDir.x));
        player.addVelocity(
            localMomentum.x * SWING_FORCE,
            localMomentum.y * SWING_FORCE,
            localMomentum.z * SWING_FORCE
        );
        player.velocityModified = true;
    }

    public static void tick(MinecraftServer server) {
        cachedServer = server;
        if (ropesById.isEmpty()) return;

        List<UUID> ropeIds = new ArrayList<>(ropesById.keySet());

        for (UUID ropeId : ropeIds) {
            RopeState rope = ropesById.get(ropeId);
            if (rope == null) continue;

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(rope.owner);
            if (player == null) {
                detach(ropeId);
                continue;
            }

            Entity anchorEntity = resolveAnchor(player.getServerWorld(), rope);
            if (rope.anchorEntityId != RopeState.NO_ANCHOR_ENTITY && anchorEntity == null) {
                detach(ropeId);
                continue;
            }
            if (anchorEntity != null) {
                rope.anchor = anchorEntity.getBoundingBox().getCenter();
            }

            if (!rope.leash) {
                if (player.isFallFlying()) {
                    rope.playerFlightTicks += 1;
                    if (rope.playerFlightTicks >= ELYTRA_TIME_LIMIT) {
                        detach(ropeId);
                        continue;
                    }
                } else {
                    rope.playerFlightTicks = 0;
                }
            }

            if (rope.leash && anchorEntity != null) {
                applyLeashPullOnTarget(player, rope, anchorEntity);
            } else {
                applyLeashConstraint(player, rope);
            }
        }
    }

    private static Entity resolveAnchor(ServerWorld world, RopeState rope) {
        if (rope.anchorEntityId == RopeState.NO_ANCHOR_ENTITY) return null;
        Entity e = world.getEntityById(rope.anchorEntityId);
        if (e != null && !e.isRemoved()) return e;
        if (world.getServer() == null) return null;
        for (ServerWorld w : world.getServer().getWorlds()) {
            Entity found = w.getEntityById(rope.anchorEntityId);
            if (found != null && !found.isRemoved()) return found;
        }
        return null;
    }

    private static void sendVerletLength(MinecraftServer server, RopeState rope) {
        if (server == null) return;
        RopeVerletLengthPayload payload = new RopeVerletLengthPayload(rope.ropeId, rope.length);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    public static void removeAll() {
        ropesById.clear();
        ownerIndex.clear();
    }
}
