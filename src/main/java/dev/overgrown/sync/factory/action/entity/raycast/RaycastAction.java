package dev.overgrown.sync.factory.action.entity.raycast;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.raycast.RaycastUtil;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleEffect;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RaycastAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        // Direction
        Vec3d rayDirection;
        if (data.isPresent("direction")) {
            Vec3d rawDir = data.get("direction");
            Space space   = data.get("space");
            Vector3f vec = new Vector3f((float) rawDir.x, (float) rawDir.y, (float) rawDir.z);
            space.toGlobal(vec, entity);
            rayDirection = new Vec3d(vec.x, vec.y, vec.z).normalize();
        } else {
            rayDirection = entity.getRotationVec(1).normalize();
        }

        Vec3d origin = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());

        // Distances
        double globalDist = data.isPresent("distance")
                ? (double)(float) data.get("distance")
                : RaycastUtil.getMaxDefaultDistance(entity);

        double blockDist = data.isPresent("block_distance")
                ? (double)(float) data.get("block_distance")
                : (data.isPresent("distance")
                ? globalDist
                : RaycastUtil.getBlockReachDistance(entity));

        double entityDist = data.isPresent("entity_distance")
                ? (double)(float) data.get("entity_distance")
                : (data.isPresent("distance")
                ? globalDist
                : RaycastUtil.getAttackRangeDistance(entity));

        Vec3d blockTarget  = origin.add(rayDirection.multiply(blockDist));
        Vec3d entityTarget = origin.add(rayDirection.multiply(entityDist));

        // before_action
        data.<Consumer<Entity>>ifPresent("before_action", a -> a.accept(entity));

        boolean pierce = data.getBoolean("pierce");

        // Entity Raycast
        List<EntityHitResult> entityHits = new ArrayList<>();
        if (data.getBoolean("entity")) {
            ConditionFactory<Pair<Entity, Entity>>.Instance biCond = data.get("bientity_condition");
            if (pierce) {
                entityHits = performPiercingEntityRaycast(entity, origin, entityTarget, biCond);
            } else {
                EntityHitResult ehr = performEntityRaycast(entity, origin, entityTarget, biCond);
                if (ehr != null) entityHits.add(ehr);
            }
        }

        // Block Raycast
        BlockHitResult blockHit = null;
        if (data.getBoolean("block")) {
            BlockHitResult bhr = performBlockRaycast(entity, origin, blockTarget,
                    data.get("shape_type"), data.get("fluid_handling"));
            if (bhr.getType() != HitResult.Type.MISS) blockHit = bhr;
        }

        // Determine Closest Hit
        HitResult primaryHit = null;
        EntityHitResult firstEntityHit = entityHits.isEmpty() ? null : entityHits.get(0);

        if (firstEntityHit != null && blockHit != null) {
            primaryHit = firstEntityHit.squaredDistanceTo(entity) <= blockHit.squaredDistanceTo(entity)
                    ? firstEntityHit : blockHit;
        } else if (firstEntityHit != null) {
            primaryHit = firstEntityHit;
        } else if (blockHit != null) {
            primaryHit = blockHit;
        }

        boolean didHit = primaryHit != null;

        // command_along_ray
        if (data.isPresent("command_along_ray")) {
            boolean onlyOnHit = data.getBoolean("command_along_ray_only_on_hit");
            if (!onlyOnHit || didHit) {
                Vec3d commandEnd = didHit ? primaryHit.getPos()
                        : origin.add(rayDirection.multiply(Math.max(blockDist, entityDist)));
                executeStepCommands(entity, origin, commandEnd,
                        data.getString("command_along_ray"),
                        data.getFloat("command_step"));
            }
        }

        // Particle Along Ray
        if (data.isPresent("particle") && entity.getWorld() instanceof ServerWorld sw) {
            ParticleEffect particle = data.get("particle");
            float spacing = data.getFloat("spacing");
            if (spacing > 0f) {
                Vec3d end = didHit ? primaryHit.getPos()
                        : origin.add(rayDirection.multiply(Math.max(blockDist, entityDist)));
                double totalLen = origin.distanceTo(end);
                for (double d = 0; d < totalLen; d += spacing) {
                    Vec3d p = origin.add(rayDirection.multiply(d));
                    sw.spawnParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
            }
        }

        // Hit Branch
        if (didHit) {
            // command_at_hit
            if (data.isPresent("command_at_hit")) {
                Vec3d offsetDir = rayDirection;
                double offset = 0;
                Vec3d hitPos = primaryHit.getPos();
                if (data.isPresent("command_hit_offset")) {
                    offset = data.getFloat("command_hit_offset");
                } else {
                    if (primaryHit instanceof BlockHitResult bhr) {
                        Direction side = bhr.getSide();
                        if (side == Direction.DOWN)       { offset = entity.getHeight(); }
                        else if (side == Direction.UP)    { offset = 0; }
                        else {
                            offset = entity.getWidth() / 2;
                            offsetDir = new Vec3d(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ()).multiply(-1);
                        }
                    }
                    offset += 0.05;
                }
                Vec3d at = hitPos.subtract(offsetDir.multiply(offset));
                executeCommandAtHit(entity, at, data.getString("command_at_hit"));
            }

            // block_condition + block_action
            if (primaryHit instanceof BlockHitResult bhr) {
                if (data.isPresent("block_action")) {
                    ActionFactory<Triple<World, BlockPos, Direction>>.Instance blockAction = data.get("block_action");
                    blockAction.accept(Triple.of(entity.getWorld(), bhr.getBlockPos(), bhr.getSide()));
                }
            }

            // bientity_action on all pierced entities (or just the first if not piercing)
            if (data.isPresent("bientity_action")) {
                ActionFactory<Pair<Entity, Entity>>.Instance bientityAction = data.get("bientity_action");
                for (EntityHitResult ehr : entityHits) {
                    // Only act on entities that were closer than the block hit (if any)
                    if (blockHit == null
                            || ehr.squaredDistanceTo(entity) <= blockHit.squaredDistanceTo(entity)) {
                        bientityAction.accept(new Pair<>(entity, ehr.getEntity()));
                    }
                }
            }

            data.<Consumer<Entity>>ifPresent("hit_action", a -> a.accept(entity));

            // Miss Branch
        } else {
            data.<Consumer<Entity>>ifPresent("miss_action", a -> a.accept(entity));
        }
    }

    // Ray Casting Helpers
    private static BlockHitResult performBlockRaycast(Entity source, Vec3d origin, Vec3d target,
                                                      RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling) {
        return source.getWorld().raycast(new RaycastContext(origin, target, shapeType, fluidHandling, source));
    }

    /** Returns the first (closest) entity hit, or {@code null} on miss. */
    private static EntityHitResult performEntityRaycast(Entity source, Vec3d origin, Vec3d target,
                                                        ConditionFactory<Pair<Entity, Entity>>.Instance biEntityCondition) {
        Vec3d ray = target.subtract(origin);
        Box box = source.getBoundingBox().stretch(ray).expand(1.0, 1.0, 1.0);
        return net.minecraft.entity.projectile.ProjectileUtil.raycast(
                source, origin, target, box,
                e -> !e.isSpectator() && (biEntityCondition == null || biEntityCondition.test(new Pair<>(source, e))),
                ray.lengthSquared());
    }

    /** Returns ALL entities along the ray, sorted by ascending distance. */
    private static List<EntityHitResult> performPiercingEntityRaycast(Entity source, Vec3d origin, Vec3d target,
                                                                      ConditionFactory<Pair<Entity, Entity>>.Instance biEntityCondition) {
        Vec3d ray = target.subtract(origin);
        Box box = source.getBoundingBox().stretch(ray).expand(1.0, 1.0, 1.0);

        List<EntityHitResult> results = new ArrayList<>();
        for (Entity candidate : source.getWorld().getOtherEntities(source, box,
                e -> !e.isSpectator() && (biEntityCondition == null || biEntityCondition.test(new Pair<>(source, e))))) {
            Box candidateBox = candidate.getBoundingBox().expand(candidate.getTargetingMargin());
            Optional<Vec3d> intersection = candidateBox.raycast(origin, target);
            if (intersection.isPresent()) {
                results.add(new EntityHitResult(candidate, intersection.get()));
            }
        }
        results.sort(Comparator.comparingDouble(r -> r.squaredDistanceTo(source)));
        return results;
    }

    // Command Helpers
    private static void executeStepCommands(Entity entity, Vec3d origin, Vec3d target,
                                            String command, float step) {
        MinecraftServer server = entity.getWorld().getServer();
        if (server == null) return;
        Vec3d dir = target.subtract(origin).normalize();
        double length = origin.distanceTo(target);
        for (double cur = 0; cur < length; cur += step) {
            server.getCommandManager().executeWithPrefix(
                    buildSource(entity, origin.add(dir.multiply(cur))), command);
        }
    }

    private static void executeCommandAtHit(Entity entity, Vec3d hitPos, String command) {
        MinecraftServer server = entity.getWorld().getServer();
        if (server == null) return;
        server.getCommandManager().executeWithPrefix(buildSource(entity, hitPos), command);
    }

    private static ServerCommandSource buildSource(Entity entity, Vec3d pos) {
        boolean validOutput = !(entity instanceof ServerPlayerEntity spe)
                || spe.networkHandler != null;
        return new ServerCommandSource(
                io.github.apace100.apoli.Apoli.config.executeCommand.showOutput && validOutput
                        ? entity : CommandOutput.DUMMY,
                pos,
                entity.getRotationClient(),
                entity.getWorld() instanceof ServerWorld sw ? sw : null,
                io.github.apace100.apoli.Apoli.config.executeCommand.permissionLevel,
                entity.getName().getString(),
                entity.getDisplayName(),
                entity.getWorld().getServer(),
                entity);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("raycast"),
                new SerializableData()
                        // core
                        .add("distance", SerializableDataTypes.FLOAT, null)
                        .add("block", SerializableDataTypes.BOOLEAN, true)
                        .add("entity", SerializableDataTypes.BOOLEAN, true)
                        .add("shape_type", SerializableDataTypes.SHAPE_TYPE, RaycastContext.ShapeType.VISUAL)
                        .add("fluid_handling", SerializableDataTypes.FLUID_HANDLING, RaycastContext.FluidHandling.ANY)
                        // direction
                        .add("space", ApoliDataTypes.SPACE, Space.WORLD)
                        .add("direction", SerializableDataTypes.VECTOR, null)
                        // pierce + particle
                        .add("pierce", SerializableDataTypes.BOOLEAN, false)
                        .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE, null)
                        .add("spacing", SerializableDataTypes.FLOAT, 0.5f)
                        // per-type distances
                        .add("entity_distance", SerializableDataTypes.FLOAT, null)
                        .add("block_distance", SerializableDataTypes.FLOAT, null)
                        // conditions
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                        // actions
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                        .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("hit_action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("miss_action", ApoliDataTypes.ENTITY_ACTION, null)
                        // commands
                        .add("command_at_hit", SerializableDataTypes.STRING, null)
                        .add("command_hit_offset", SerializableDataTypes.FLOAT, null)
                        .add("command_along_ray", SerializableDataTypes.STRING, null)
                        .add("command_step", SerializableDataTypes.FLOAT, 1.0f)
                        .add("command_along_ray_only_on_hit", SerializableDataTypes.BOOLEAN, false),
                RaycastAction::action
        );
    }
}