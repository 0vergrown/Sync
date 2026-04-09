package dev.overgrown.sync.factory.condition.entity.raycast;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.raycast.RaycastUtil;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;

public class RaycastCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        // Direction
        Vec3d rayDirection;
        if (data.isPresent("direction")) {
            Vec3d rawDir = data.get("direction");
            Space space  = data.get("space");
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

        // Entity Raycast
        HitResult hitResult = null;
        if (data.getBoolean("entity")) {
            ConditionFactory<Pair<Entity, Entity>>.Instance matchCond = data.get("match_bientity_condition");
            hitResult = performEntityRaycast(entity, origin, entityTarget, matchCond);
        }

        // Block Raycast
        if (data.getBoolean("block")) {
            BlockHitResult blockHit = performBlockRaycast(entity, origin, blockTarget,
                    data.get("shape_type"), data.get("fluid_handling"));
            if (blockHit.getType() != HitResult.Type.MISS) {
                if (hitResult == null || hitResult.getType() == HitResult.Type.MISS
                        || blockHit.squaredDistanceTo(entity) < hitResult.squaredDistanceTo(entity)) {
                    hitResult = blockHit;
                }
            }
        }

        // Evaluate Result
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            return false;
        }

        // block_condition
        if (hitResult instanceof BlockHitResult bhr && data.isPresent("block_condition")) {
            CachedBlockPosition cbp = new CachedBlockPosition(entity.getWorld(), bhr.getBlockPos(), true);
            return data.<java.util.function.Predicate<CachedBlockPosition>>get("block_condition").test(cbp);
        }

        // hit_bientity_condition
        if (hitResult instanceof EntityHitResult ehr && data.isPresent("hit_bientity_condition")) {
            return data.<java.util.function.Predicate<Pair<Entity, Entity>>>get("hit_bientity_condition")
                    .test(new Pair<>(entity, ehr.getEntity()));
        }

        return true;
    }

    // Helpers
    private static BlockHitResult performBlockRaycast(Entity source, Vec3d origin, Vec3d target,
                                                      RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling) {
        return source.getWorld().raycast(new RaycastContext(origin, target, shapeType, fluidHandling, source));
    }

    private static EntityHitResult performEntityRaycast(Entity source, Vec3d origin, Vec3d target,
                                                        ConditionFactory<Pair<Entity, Entity>>.Instance biEntityCondition) {
        Vec3d ray = target.subtract(origin);
        Box box = source.getBoundingBox().stretch(ray).expand(1.0, 1.0, 1.0);
        return ProjectileUtil.raycast(
                source, origin, target, box,
                e -> !e.isSpectator() && (biEntityCondition == null || biEntityCondition.test(new Pair<>(source, e))),
                ray.lengthSquared());
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
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
                        // per-type distances
                        .add("entity_distance", SerializableDataTypes.FLOAT, null)
                        .add("block_distance", SerializableDataTypes.FLOAT, null)
                        // filter conditions (narrow which entities count as a hit)
                        .add("match_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        // conditions on what was actually hit
                        .add("hit_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
                RaycastCondition::condition
        );
    }
}