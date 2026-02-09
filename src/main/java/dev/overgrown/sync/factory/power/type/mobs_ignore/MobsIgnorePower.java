package dev.overgrown.sync.factory.power.type.mobs_ignore;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class MobsIgnorePower extends Power {
    private final Predicate<Entity> mobCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final boolean provokable;
    private final Set<UUID> provokedMobs = new HashSet<>();

    public MobsIgnorePower(PowerType<?> type, LivingEntity entity,
                           ConditionFactory<Entity>.Instance mobCondition,
                           ConditionFactory<Pair<Entity, Entity>>.Instance biEntityCondition,
                           boolean provokable) {
        super(type, entity);
        this.mobCondition = mobCondition;
        this.biEntityCondition = biEntityCondition;
        this.provokable = provokable;
    }

    public boolean shouldIgnore(Entity mob) {
        if (!isActive()) {
            return false;
        }

        // Check if mob is provoked (only if provokable is true)
        if (provokable && provokedMobs.contains(mob.getUuid())) {
            return false; // Don't ignore if provoked and provokable is enabled
        }

        // Check conditions
        if (mobCondition != null && !mobCondition.test(mob)) {
            return false;
        }
        if (biEntityCondition != null && !biEntityCondition.test(new Pair<>(mob, entity))) {
            return false;
        }

        return true;
    }

    public void provokeMob(Entity mob) {
        if (provokable) {
            provokedMobs.add(mob.getUuid());
        }
    }

    public void clearProvokedMobs() {
        provokedMobs.clear();
    }

    public boolean isProvoked(Entity mob) {
        return provokedMobs.contains(mob.getUuid());
    }

    public boolean isProvokable() {
        return provokable;
    }

    public static PowerFactory<MobsIgnorePower> getFactory() {
        return new PowerFactory<MobsIgnorePower>(
                Sync.identifier("mobs_ignore"),
                new SerializableData()
                        .add("mob_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("provokable", SerializableDataTypes.BOOLEAN, true), // Default to true
                data -> (type, entity) -> new MobsIgnorePower(
                        type,
                        entity,
                        data.get("mob_condition"),
                        data.get("bientity_condition"),
                        data.getBoolean("provokable")
                )
        ).allowCondition();
    }
}