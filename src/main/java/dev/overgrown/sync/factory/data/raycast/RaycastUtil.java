package dev.overgrown.sync.factory.data.raycast;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Resolves default raycast distances via {@link ReachEntityAttributes}.
 *
 * <p>{@code ReachEntityAttributes.REACH} and {@code ATTACK_RANGE} are <em>additive</em>
 * attributes with a base value of {@code 0.0}, so their helper methods accept the vanilla
 * baseline and layer the modifier on top.  For non-{@link LivingEntity} targets (e.g.
 * bare {@link net.minecraft.entity.Entity} references) we fall back to the vanilla
 * constants directly.
 */
public final class RaycastUtil {

    // These are the values used as the "base" passed to ReachEntityAttributes,
    // and also the final fallback for non-LivingEntity callers.
    public static final double BASE_BLOCK_REACH_SURVIVAL  = 4.5;
    public static final double BASE_BLOCK_REACH_CREATIVE  = 5.0;
    public static final double BASE_ATTACK_RANGE_SURVIVAL = 3.0;
    public static final double BASE_ATTACK_RANGE_CREATIVE = 6.0;

    private RaycastUtil() {}

    // Public helpers
    /**
     * Returns the effective block-reach distance for {@code entity}, including
     * any value contributed by {@link ReachEntityAttributes#REACH}.
     */
    public static double getBlockReachDistance(Entity entity) {
        double base = isCreative(entity) ? BASE_BLOCK_REACH_CREATIVE : BASE_BLOCK_REACH_SURVIVAL;
        if (entity instanceof LivingEntity living) {
            return ReachEntityAttributes.getReachDistance(living, base);
        }
        return base;
    }

    /**
     * Returns the effective entity-attack range for {@code entity}, including
     * any value contributed by {@link ReachEntityAttributes#ATTACK_RANGE}.
     */
    public static double getAttackRangeDistance(Entity entity) {
        double base = isCreative(entity) ? BASE_ATTACK_RANGE_CREATIVE : BASE_ATTACK_RANGE_SURVIVAL;
        if (entity instanceof LivingEntity living) {
            return ReachEntityAttributes.getAttackRange(living, base);
        }
        return base;
    }

    /**
     * Returns the larger of {@link #getBlockReachDistance} and
     * {@link #getAttackRangeDistance}, used when a single {@code distance}
     * field must cover both.
     */
    public static double getMaxDefaultDistance(Entity entity) {
        return Math.max(getBlockReachDistance(entity), getAttackRangeDistance(entity));
    }

    // Internal helpers
    private static boolean isCreative(Entity entity) {
        return entity instanceof PlayerEntity p && p.getAbilities().creativeMode;
    }
}