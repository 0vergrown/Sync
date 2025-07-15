package dev.overgrown.sync.factory.condition.entity;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.power.type.EntitySetPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class InEntitySetCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(actorAndTarget.getLeft()).orElse(null);
        PowerType<?> powerType = data.get("set");

        if (component == null || powerType == null || !(component.getPower(powerType) instanceof EntitySetPower entitySetPower)) {
            return false;
        }

        return entitySetPower.contains(actorAndTarget.getRight());

    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("in_set"),
                new SerializableData()
                        .add("set", ApoliDataTypes.POWER_TYPE),
                InEntitySetCondition::condition
        );
    }
}