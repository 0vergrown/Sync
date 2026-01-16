package dev.overgrown.sync.factory.power.type;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import java.util.function.Predicate;

public class MobsIgnorePower extends Power {
    private final Predicate<Entity> mobCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    public MobsIgnorePower(PowerType<?> type, LivingEntity entity,
                           io.github.apace100.apoli.power.factory.condition.ConditionFactory<Entity>.Instance mobCondition,
                           io.github.apace100.apoli.power.factory.condition.ConditionFactory<Pair<Entity, Entity>>.Instance biEntityCondition) {
        super(type, entity);
        this.mobCondition = mobCondition;
        this.biEntityCondition = biEntityCondition;
    }

    public boolean shouldIgnore(Entity mob) {
        if (mobCondition != null && !mobCondition.test(mob)) {
            return false;
        }
        if (biEntityCondition != null && !biEntityCondition.test(new Pair<>(mob, entity))) {
            return false;
        }
        return isActive();
    }

    public static PowerFactory<MobsIgnorePower> getFactory() {
        return new PowerFactory<MobsIgnorePower>(
                Sync.identifier("mobs_ignore"),
                new SerializableData()
                        .add("mob_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                data -> (type, entity) -> new MobsIgnorePower(
                        type,
                        entity,
                        data.get("mob_condition"),
                        data.get("bientity_condition")
                )
        ).allowCondition();
    }
}