package dev.overgrown.sync.factory.power.type.body_part_damage_modifier;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils.BodyPartModifierEntry;
import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils.HitLocationTracker;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BodyPartDamageModifierPower extends Power {
    private static final Logger LOGGER = LoggerFactory.getLogger("sync/BodyPartDmgMod");

    private final List<BodyPartModifierEntry> entries;
    private final ConditionFactory<Pair<DamageSource, Float>>.Instance damageCondition;
    private final boolean requireHitData;
    private final boolean showHitLocation;

    public BodyPartDamageModifierPower(PowerType<?> type,
                                       LivingEntity entity,
                                       List<BodyPartModifierEntry> entries,
                                       ConditionFactory<Pair<DamageSource, Float>>.Instance damageCondition,
                                       boolean requireHitData,
                                       boolean showHitLocation) {
        super(type, entity);
        this.entries = entries;
        this.damageCondition = damageCondition;
        this.requireHitData = requireHitData;
        this.showHitLocation = showHitLocation;
    }

    public float apply(float amount, DamageSource source) {
        if (!isActive()) return amount;

        if (damageCondition != null && !damageCondition.test(new Pair<>(source, amount))) {
            return amount;
        }

        Vec3d norm = resolveHitLocation(source);
        if (norm == null) return amount;

        double xNorm = norm.x;
        double yNorm = norm.y;
        double zNorm = norm.z;

        double result = amount;
        String matchedZone = null;
        for (BodyPartModifierEntry entry : entries) {
            if (entry.getRegion().contains(xNorm, yNorm, zNorm)) {
                result = ModifierUtil.applyModifiers(entity, entry.getModifiers(), result);
                if (showHitLocation && matchedZone == null) {
                    matchedZone = classifyZone(xNorm, yNorm, zNorm);
                }
            }
        }

        if (showHitLocation) {
            String zone = matchedZone != null ? matchedZone : classifyZone(xNorm, yNorm, zNorm);
            String msg = String.format("[BodyPart] Zone=%s x=%.2f y=%.2f z=%.2f | dmg %.1f -> %.1f",
                    zone, xNorm, yNorm, zNorm, amount, result);
            LOGGER.info(msg);
            // Send to power holder
            if (entity instanceof ServerPlayerEntity spe) {
                spe.sendMessage(Text.literal(msg), false);
            }
            // Also send to the attacker so the person swinging/shooting can see it
            if (source != null && source.getAttacker() instanceof ServerPlayerEntity attacker
                    && attacker != entity) {
                attacker.sendMessage(Text.literal(msg), false);
            }
        }

        return (float) Math.max(0.0, result);
    }

    /**
     * Determines the normalized hit location based on the damage source.
     * <p>
     * Recorded hit data (from melee ray-cast or projectile impact) is used first.
     * If none is available, the source type determines the zone:
     * <ul>
     *   <li>Fall / hot floor / stalagmite -> feet or legs</li>
     *   <li>Drowning / starvation -> torso</li>
     *   <li>Falling objects (anvil, block, stalactite, fly-into-wall) -> head</li>
     *   <li>Everything else (mob attacks, explosions, etc.) -> random zone</li>
     * </ul>
     */
    private Vec3d resolveHitLocation(DamageSource source) {
        // Check for recorded hit data (set by MeleeAttackBodyHitMixin or ProjectileBodyHitMixin)
        Vec3d tracked = HitLocationTracker.getAndClear(entity);
        if (tracked != null) return tracked;

        // No tracked data, if the power requires it (projectile-only powers), bail out
        if (requireHitData) return null;

        if (source == null) return randomBodyZone();

        // Route by damage source type for cases with no tracker data
        // (mob melee, environmental, etc.)
        if (source.isOf(DamageTypes.FALL)
                || source.isOf(DamageTypes.HOT_FLOOR)
                || source.isOf(DamageTypes.STALAGMITE)) {
            return entity.getRandom().nextBoolean()
                    ? new Vec3d(0.0, 0.09, 0.0)   // feet
                    : new Vec3d(0.0, 0.34, 0.0);  // legs
        }

        if (source.isOf(DamageTypes.DROWN) || source.isOf(DamageTypes.STARVE)) {
            return new Vec3d(0.0, 0.69, 0.0); // torso
        }

        if (source.isOf(DamageTypes.FLY_INTO_WALL)
                || source.isOf(DamageTypes.FALLING_ANVIL)
                || source.isOf(DamageTypes.FALLING_BLOCK)
                || source.isOf(DamageTypes.FALLING_STALACTITE)) {
            return new Vec3d(0.0, 0.94, 0.0); // head
        }

        // Everything else (mob melee, explosion, lava, untracked projectile, etc.)
        return randomBodyZone();
    }

    /**
     * Picks a random body zone with equal probability per body part
     * (8 parts: head, torso, L-arm, R-arm, L-leg, R-leg, L-foot, R-foot).
     */
    private Vec3d randomBodyZone() {
        int zone = entity.getRandom().nextInt(8);
        return switch (zone) {
            case 0 -> new Vec3d(0.0, 0.94, 0.0); // head
            case 1 -> new Vec3d(0.0, 0.69, 0.0); // torso
            case 2 -> new Vec3d(0.90, 0.74, 0.0); // left arm
            case 3 -> new Vec3d(-0.90, 0.74, 0.0); // right arm
            case 4, 5 -> new Vec3d(0.0, 0.34, 0.0); // legs (2 slots)
            case 6, 7 -> new Vec3d(0.0, 0.09, 0.0); // feet (2 slots)
            default -> new Vec3d(0.0, 0.69, 0.0);
        };
    }

    private static String classifyZone(double x, double y, double z) {
        if (y >= 0.88) return "Head";
        if (y >= 0.50) {
            if (y >= 0.60 && x > 0.80) return "Left Arm";
            if (y >= 0.60 && x < -0.80) return "Right Arm";
            return "Torso";
        }
        if (y >= 0.18) return "Legs";
        return "Feet";
    }

    public static PowerFactory<BodyPartDamageModifierPower> getFactory() {
        PowerFactory<BodyPartDamageModifierPower> factory = new PowerFactory<>(
                Sync.identifier("body_part_damage_modifier"),
                new SerializableData()
                        .add("modifiers", BodyPartModifierEntry.LIST_TYPE)
                        .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                        .add("require_hit_data", SerializableDataTypes.BOOLEAN, false)
                        .add("show_hit_location", SerializableDataTypes.BOOLEAN, false),
                data -> (type, player) -> new BodyPartDamageModifierPower(
                        type, player,
                        data.get("modifiers"),
                        data.get("damage_condition"),
                        data.getBoolean("require_hit_data"),
                        data.getBoolean("show_hit_location")
                )
        );
        factory.allowCondition();
        return factory;
    }
}