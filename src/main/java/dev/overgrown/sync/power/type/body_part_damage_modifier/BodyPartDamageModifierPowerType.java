package dev.overgrown.sync.power.type.body_part_damage_modifier;

import dev.overgrown.sync.power.type.body_part_damage_modifier.util.BodyPartModifierEntry;
import dev.overgrown.sync.power.type.body_part_damage_modifier.util.HitLocationTracker;
import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class BodyPartDamageModifierPowerType extends PowerType {

    private static final Logger LOGGER = LoggerFactory.getLogger("sync/BodyPartDmgMod");

    public static final TypedDataObjectFactory<BodyPartDamageModifierPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("modifiers", BodyPartModifierEntry.LIST_TYPE)
            .add("damage_condition", DamageCondition.DATA_TYPE.optional(), Optional.empty())
            .add("require_hit_data", SerializableDataTypes.BOOLEAN, false)
            .add("show_hit_location", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new BodyPartDamageModifierPowerType(
            data.get("modifiers"),
            data.get("damage_condition"),
            data.get("require_hit_data"),
            data.get("show_hit_location"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("modifiers", powerType.entries)
            .set("damage_condition", powerType.damageCondition)
            .set("require_hit_data", powerType.requireHitData)
            .set("show_hit_location", powerType.showHitLocation)
    );

    private final List<BodyPartModifierEntry> entries;
    private final Optional<DamageCondition> damageCondition;
    private final boolean requireHitData;
    private final boolean showHitLocation;

    public BodyPartDamageModifierPowerType(List<BodyPartModifierEntry> entries,
                                           Optional<DamageCondition> damageCondition,
                                           boolean requireHitData,
                                           boolean showHitLocation,
                                           Optional<EntityCondition> condition) {
        super(condition);
        this.entries = entries;
        this.damageCondition = damageCondition;
        this.requireHitData = requireHitData;
        this.showHitLocation = showHitLocation;
    }

    public float apply(float amount, DamageSource source) {
        if (!isActive()) return amount;

        if (damageCondition.isPresent() && !damageCondition.get().test(source, amount)) {
            return amount;
        }

        Entity holder = getHolder();
        if (!(holder instanceof LivingEntity living)) return amount;

        Vec3d norm = resolveHitLocation(living, source);
        if (norm == null) return amount;

        double xNorm = norm.x;
        double yNorm = norm.y;
        double zNorm = norm.z;

        double result = amount;
        String matchedZone = null;
        Entity attacker = source != null ? source.getAttacker() : null;
        for (BodyPartModifierEntry entry : entries) {
            if (entry.getRegion().contains(xNorm, yNorm, zNorm)) {
                result = ModifierUtil.applyModifiers(holder, entry.getModifiers(), result);
                if (entry.getBientityAction().isPresent() && attacker != null) {
                    entry.getBientityAction().get().accept(new BiEntityActionContext(attacker, holder));
                }
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
            if (holder instanceof ServerPlayerEntity spe) {
                spe.sendMessage(Text.literal(msg), false);
            }
            if (attacker instanceof ServerPlayerEntity attackerPlayer && attackerPlayer != holder) {
                attackerPlayer.sendMessage(Text.literal(msg), false);
            }
        }

        return (float) Math.max(0.0, result);
    }

    private Vec3d resolveHitLocation(LivingEntity living, DamageSource source) {
        Vec3d tracked = HitLocationTracker.getAndClear(living);
        if (tracked != null) return tracked;

        if (requireHitData) return null;
        if (source == null) return randomBodyZone(living);

        if (source.isOf(DamageTypes.FALL)
            || source.isOf(DamageTypes.HOT_FLOOR)
            || source.isOf(DamageTypes.STALAGMITE)) {
            return living.getRandom().nextBoolean()
                ? new Vec3d(0.0, 0.09, 0.0)
                : new Vec3d(0.0, 0.34, 0.0);
        }

        if (source.isOf(DamageTypes.DROWN) || source.isOf(DamageTypes.STARVE)) {
            return new Vec3d(0.0, 0.69, 0.0);
        }

        if (source.isOf(DamageTypes.FLY_INTO_WALL)
            || source.isOf(DamageTypes.FALLING_ANVIL)
            || source.isOf(DamageTypes.FALLING_BLOCK)
            || source.isOf(DamageTypes.FALLING_STALACTITE)) {
            return new Vec3d(0.0, 0.94, 0.0);
        }

        return randomBodyZone(living);
    }

    private static Vec3d randomBodyZone(LivingEntity entity) {
        int zone = entity.getRandom().nextInt(8);
        return switch (zone) {
            case 0 -> new Vec3d(0.0, 0.94, 0.0);
            case 1 -> new Vec3d(0.0, 0.69, 0.0);
            case 2 -> new Vec3d(0.90, 0.74, 0.0);
            case 3 -> new Vec3d(-0.90, 0.74, 0.0);
            case 4, 5 -> new Vec3d(0.0, 0.34, 0.0);
            case 6, 7 -> new Vec3d(0.0, 0.09, 0.0);
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

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.BODY_PART_DAMAGE_MODIFIER;
    }
}
