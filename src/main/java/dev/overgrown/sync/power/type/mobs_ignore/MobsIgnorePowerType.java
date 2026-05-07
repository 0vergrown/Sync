package dev.overgrown.sync.power.type.mobs_ignore;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MobsIgnorePowerType extends PowerType {

    public static final TypedDataObjectFactory<MobsIgnorePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("mob_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("provokable", SerializableDataTypes.BOOLEAN, true),
        (data, condition) -> new MobsIgnorePowerType(
            data.get("mob_condition"),
            data.get("bientity_condition"),
            data.get("provokable"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("mob_condition", powerType.mobCondition)
            .set("bientity_condition", powerType.biEntityCondition)
            .set("provokable", powerType.provokable)
    );

    private final Optional<EntityCondition> mobCondition;
    private final Optional<BiEntityCondition> biEntityCondition;
    private final boolean provokable;
    private final Set<UUID> provokedMobs = new HashSet<>();

    public MobsIgnorePowerType(Optional<EntityCondition> mobCondition,
                               Optional<BiEntityCondition> biEntityCondition,
                               boolean provokable,
                               Optional<EntityCondition> condition) {
        super(condition);
        this.mobCondition = mobCondition;
        this.biEntityCondition = biEntityCondition;
        this.provokable = provokable;
    }

    public boolean shouldIgnore(Entity mob) {
        if (!isActive()) return false;
        if (provokable && provokedMobs.contains(mob.getUuid())) return false;
        if (mobCondition.isPresent() && !mobCondition.get().test(mob)) return false;
        if (biEntityCondition.isPresent() && !biEntityCondition.get().test(mob, getHolder())) return false;
        return true;
    }

    public void provokeMob(Entity mob) {
        if (provokable) provokedMobs.add(mob.getUuid());
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

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.MOBS_IGNORE;
    }
}
