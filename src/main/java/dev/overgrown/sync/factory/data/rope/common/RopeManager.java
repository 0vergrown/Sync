package dev.overgrown.sync.factory.data.rope.common;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
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

import static dev.overgrown.sync.factory.data.rope.common.RopeConstants.*;

public class RopeManager {

    // ropeId -> RopeState. This is the canonical storage; all lookups funnel
    // through here. ownerIndex is a secondary index kept in sync with ropesById
    // so we can cheaply enumerate ropes belonging to a single owner.
    private static final Map<UUID, RopeState> ropesById = new HashMap<>();
    private static final Map<UUID, Set<UUID>> ownerIndex = new HashMap<>();

    // Cached server reference updated every tick. Used by detach paths that
    // don't have direct access to a player/server (e.g. scheduled detaches
    // from disguise cleanup). Null before the first tick runs.
    private static MinecraftServer cachedServer;

    // Tolerance (in blocks) used when matching a rope to a block anchor
    // position. Block raycasts return an exact hit location, but re-raycasts
    // on the same block face won't always produce the exact same Vec3d, so
    // we match within a small radius.
    private static final double ANCHOR_MATCH_TOLERANCE = 0.6;

    // ------ Rope Management Functions ------

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

        // Elytra interaction only applies to self-swinging (non-leash) ropes so
        // that a lead-style tow doesn't get hijacked by the elytra shorten-rope
        // boost. Only applied to the *first* attached rope on this activation —
        // subsequent simultaneous attaches shouldn't compound the boost.
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
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(state.ropeId);
        buf.writeUuid(state.owner);
        buf.writeDouble(state.anchor.x);
        buf.writeDouble(state.anchor.y);
        buf.writeDouble(state.anchor.z);
        buf.writeDouble(state.length);
        buf.writeFloat(state.maxLength);
        buf.writeIdentifier(state.texture);
        buf.writeVarInt(state.anchorEntityId);
        buf.writeBoolean(state.leash);
        for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, RopePackets.ROPE_CREATE, buf);
        }
    }

    private static void broadcastDelete(MinecraftServer server, UUID ropeId) {
        if (server == null) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(ropeId);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, RopePackets.ROPE_DELETE, buf);
        }
    }

    /**
     * Detach a single rope by its id. Silent no-op if the id is unknown.
     */
    public static void detach(UUID ropeId) {
        RopeState removed = ropesById.remove(ropeId);
        if (removed == null) return;

        Set<UUID> ownerRopes = ownerIndex.get(removed.owner);
        if (ownerRopes != null) {
            ownerRopes.remove(ropeId);
            if (ownerRopes.isEmpty()) ownerIndex.remove(removed.owner);
        }

        // Look up any online player to grab a server reference for broadcast.
        // Using the rope owner isn't safe because they may have disconnected.
        MinecraftServer server = findAnyServer();
        broadcastDelete(server, ropeId);
    }

    /**
     * Detach every rope owned by the player. Used for the "detach_all" mode
     * and the legacy toggle-with-no-target behavior.
     */
    public static void detachAll(UUID ownerUuid) {
        Set<UUID> ropeIds = ownerIndex.remove(ownerUuid);
        if (ropeIds == null || ropeIds.isEmpty()) return;

        MinecraftServer server = findAnyServer();
        for (UUID ropeId : ropeIds) {
            ropesById.remove(ropeId);
            broadcastDelete(server, ropeId);
        }
    }

    /**
     * Compatibility shim: detaches every rope owned by the player, matching
     * the previous single-rope behavior of this method's predecessor.
     */
    public static void detach(ServerPlayerEntity player) {
        detachAll(player.getUuid());
    }

    /**
     * Detach the rope (if any) that {@code owner} has attached to the given
     * anchor entity id.
     */
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

    /**
     * Detach the rope (if any) that {@code owner} has attached to a static
     * block anchor near the given position. Only considers non-entity anchors.
     */
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

    /**
     * True when {@code ownerUuid} owns at least one rope (any kind).
     */
    public static boolean has(UUID ownerUuid) {
        Set<UUID> ropeIds = ownerIndex.get(ownerUuid);
        return ropeIds != null && !ropeIds.isEmpty();
    }

    /**
     * True when {@code ownerUuid} owns a rope whose anchor is the given entity.
     */
    public static boolean hasRopeToEntity(UUID ownerUuid, int anchorEntityId) {
        Set<UUID> ropeIds = ownerIndex.get(ownerUuid);
        if (ropeIds == null) return false;
        for (UUID ropeId : ropeIds) {
            RopeState state = ropesById.get(ropeId);
            if (state != null && state.anchorEntityId == anchorEntityId) return true;
        }
        return false;
    }

    /**
     * True when {@code ownerUuid} owns a rope anchored to a block position
     * near {@code anchorPos}.
     */
    public static boolean hasRopeToAnchor(UUID ownerUuid, Vec3d anchorPos) {
        Set<UUID> ropeIds = ownerIndex.get(ownerUuid);
        if (ropeIds == null) return false;
        double matchSq = ANCHOR_MATCH_TOLERANCE * ANCHOR_MATCH_TOLERANCE;
        for (UUID ropeId : ropeIds) {
            RopeState state = ropesById.get(ropeId);
            if (state == null) continue;
            if (state.anchorEntityId != RopeState.NO_ANCHOR_ENTITY) continue;
            if (state.anchor.squaredDistanceTo(anchorPos) <= matchSq) return true;
        }
        return false;
    }

    /**
     * Count how many of {@code ownerUuid}'s ropes match the given filter.
     */
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

    private static MinecraftServer findAnyServer() {
        return cachedServer;
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

    // When the rope is in leash mode, pull the ANCHOR entity toward the player
    // instead of pulling the player toward the anchor. No swing physics.
    private static void applyLeashPullOnTarget(ServerPlayerEntity player, RopeState rope, Entity target) {
        Vec3d playerCenter = player.getBoundingBox().getCenter();
        Vec3d targetCenter = target.getBoundingBox().getCenter();
        Vec3d delta = playerCenter.subtract(targetCenter);
        double dist = delta.length();

        if (dist <= rope.length) return;

        Vec3d dir = delta.normalize();
        double excess = dist - rope.length;

        // Spring pulling target toward player
        Vec3d correction = dir.multiply(excess * LEASH_STIFFNESS);

        // Damp target's away-from-player velocity component
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

    // ------ Player Controls Functions ------

    /**
     * Apply a length delta to every non-leash rope owned by the player.
     * Predictable multi-rope UX: scrolling adjusts all swing ropes equally.
     * Leash ropes are left untouched so they retain their natural pull length.
     */
    public static void handleChangeLength(ServerPlayerEntity player, double delta) {
        List<RopeState> owned = getByOwner(player.getUuid());
        if (owned.isEmpty()) return;

        boolean changed = false;
        for (RopeState state : owned) {
            if (state.leash) continue;
            double effectiveDelta = delta;
            // If rope is slack, increase pull rate
            double dist = player.getBoundingBox().getCenter().subtract(state.anchor).length();
            if (effectiveDelta < 0 && dist < 0.95 * state.length)
                effectiveDelta *= SLACK_PULL_RATE_MULT;
            double newLen = MathHelper.clamp(state.length + effectiveDelta, MIN_ROPE_LENGTH, state.maxLength);
            if (newLen != state.length) {
                state.length = newLen;
                sendVerletLength(player.getServer(), state);
                changed = true;
            }
        }
        // changed is informational; callers currently don't react to it.
        if (!changed) return;
    }

    public static void handleSwing(ServerPlayerEntity player, Vec3d inputDir) {
        List<RopeState> owned = getByOwner(player.getUuid());
        if (owned.isEmpty()) return;

        // Gate on at least one non-leash rope — swing force is applied once
        // per activation regardless of rope count.
        boolean anySwingRope = false;
        for (RopeState state : owned) {
            if (!state.leash) { anySwingRope = true; break; }
        }
        if (!anySwingRope) return;

        // Use full 3D look direction (pitch + yaw) so the player can aim swings
        // up/down — looking down while pressing forward adds downward momentum,
        // making pendulum-style pumping possible.
        Vec3d forward = Vec3d.fromPolar(player.getPitch(), player.getYaw());
        Vec3d right = forward.crossProduct(new Vec3d(0, 1, 0));
        if (right.lengthSquared() < 1e-6) {
            // Looking straight up/down — fall back to yaw-only
            double yawRad = Math.toRadians(player.getYaw());
            right = new Vec3d(Math.cos(yawRad), 0, Math.sin(yawRad));
        } else {
            right = right.normalize();
        }

        // Combine WASD input into world-space velocity
        Vec3d localMomentum = forward.multiply(inputDir.z).add(right.multiply(inputDir.x));

        player.addVelocity(
                localMomentum.x * SWING_FORCE,
                localMomentum.y * SWING_FORCE,
                localMomentum.z * SWING_FORCE
        );
        player.velocityModified = true;
    }

    // ------ Tick Function ------

    public static void tick(MinecraftServer server) {
        cachedServer = server;
        if (ropesById.isEmpty()) return;

        // Snapshot the rope-id set before iterating, because detach() may
        // mutate both maps from within this loop (anchor entity despawn,
        // elytra time limit).
        List<UUID> ropeIds = new ArrayList<>(ropesById.keySet());

        for (UUID ropeId : ropeIds) {
            RopeState rope = ropesById.get(ropeId);
            if (rope == null) continue;

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(rope.owner);
            if (player == null) {
                // Owner not online — drop the rope. A disconnected player's
                // ropes shouldn't persist indefinitely.
                detach(ropeId);
                continue;
            }

            // Follow anchor entity if one is set — the rope's world-space anchor
            // needs to track the entity's live position for both physics and the
            // client-side rendering target.
            Entity anchorEntity = resolveAnchor(player.getServerWorld(), rope);
            if (rope.anchorEntityId != RopeState.NO_ANCHOR_ENTITY && anchorEntity == null) {
                // Anchor entity despawned / unloaded — drop this rope.
                detach(ropeId);
                continue;
            }
            if (anchorEntity != null) {
                rope.anchor = anchorEntity.getBoundingBox().getCenter();
            }

            // Handle elytra restrictions: detach after time limit. Only applies
            // to self-swinging ropes.
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
        // Fall back to scanning every loaded world so a rope holds across
        // dimensions until the owner or target changes dimensions.
        if (world.getServer() == null) return null;
        for (ServerWorld w : world.getServer().getWorlds()) {
            Entity found = w.getEntityById(rope.anchorEntityId);
            if (found != null && !found.isRemoved()) return found;
        }
        return null;
    }

    private static void sendVerletLength(MinecraftServer server, RopeState rope) {
        if (server == null) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(rope.ropeId);
        buf.writeDouble(rope.length);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, RopePackets.ROPE_VERLET_LENGTH, buf);
        }
    }

    public static void removeAll() {
        ropesById.clear();
        ownerIndex.clear();
    }
}
