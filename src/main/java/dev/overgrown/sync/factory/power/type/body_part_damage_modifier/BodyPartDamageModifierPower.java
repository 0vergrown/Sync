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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BodyPartDamageModifierPower extends Power {

    private final List<BodyPartModifierEntry> entries;
    /**
     * Optional damage condition: Mirrors how ModifyDamageTakenPower works.
     * If present, the entire power only fires when this condition passes.
     */
    private final ConditionFactory<Pair<DamageSource, Float>>.Instance damageCondition;

    public BodyPartDamageModifierPower(PowerType<?> type,
                                       LivingEntity entity,
                                       List<BodyPartModifierEntry> entries,
                                       ConditionFactory<Pair<DamageSource, Float>>.Instance damageCondition) {
        super(type, entity);
        this.entries = entries;
        this.damageCondition = damageCondition;
    }

    /**
     * Returns the final damage amount after applying all matching per-part modifiers.
     * Call this from the mixin.
     */
    public float apply(float amount, DamageSource source) {
        if (!isActive()) return amount;

        // Check optional damage condition
        if (damageCondition != null && !damageCondition.test(new Pair<>(source, amount))) {
            return amount;
        }

        // Determine hit region
        Vec3d norm = HitLocationTracker.getAndClear(entity);
        double xNorm = norm != null ? norm.x : 0.0;
        double yNorm = norm != null ? norm.y : estimateYNorm(source);
        double zNorm = norm != null ? norm.z : 0.0;

        double result = amount;
        for (BodyPartModifierEntry entry : entries) {
            if (entry.getRegion().contains(xNorm, yNorm, zNorm)) {
                result = ModifierUtil.applyModifiers(entity, entry.getModifiers(), result);
            }
        }
        return (float) Math.max(0.0, result);
    }

    /**
     * Rough fallback when no projectile hit was recorded:
     * use the vertical gap between attacker and victim to guess the region.
     */
    private double estimateYNorm(DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker == null) return 0.6; // default to torso
        double dy = attacker.getEyeY() - entity.getY();
        double height = Math.max(entity.getHeight(), 0.001);
        return Math.max(0.0, Math.min(1.0, dy / height));
    }

    public static PowerFactory<BodyPartDamageModifierPower> getFactory() {
        PowerFactory<BodyPartDamageModifierPower> factory = new PowerFactory<>(
                Sync.identifier("body_part_damage_modifier"),
                new SerializableData()
                        .add("modifiers",        BodyPartModifierEntry.LIST_TYPE)
                        .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null),
                data -> (type, player) -> new BodyPartDamageModifierPower(
                        type, player,
                        data.get("modifiers"),
                        data.get("damage_condition")
                )
        );
        factory.allowCondition();
        return factory;
    }
}