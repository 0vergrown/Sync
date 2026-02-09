package dev.overgrown.sync.factory.action.bientity.add_to_entity_set;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.power.type.entity_set.EntitySetPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class AddToEntitySetAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        Integer timeLimit = data.get("time_limit");
        if (timeLimit != null && timeLimit <= 0) {
            // Handle invalid time_limit (e.g., log error, throw exception)
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(actorAndTarget.getLeft());
        PowerType<?> powerType = data.get("set");

        if (component == null || powerType == null || !(component.getPower(powerType) instanceof EntitySetPower entitySetPower)) {
            return;
        }

        if (entitySetPower.add(actorAndTarget.getRight(), data.get("time_limit"))) {
            PowerHolderComponent.syncPower(actorAndTarget.getLeft(), powerType);
        }

        if (entitySetPower.add(actorAndTarget.getRight(), timeLimit)) {
            PowerHolderComponent.syncPower(actorAndTarget.getLeft(), powerType);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("add_to_entity_set"),
                new SerializableData()
                        .add("set", ApoliDataTypes.POWER_TYPE)
                        .add("time_limit", SerializableDataTypes.INT, null),
                AddToEntitySetAction::action
        );
    }
}