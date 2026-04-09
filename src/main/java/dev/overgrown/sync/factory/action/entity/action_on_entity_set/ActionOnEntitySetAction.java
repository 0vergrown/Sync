package dev.overgrown.sync.factory.action.entity.action_on_entity_set;

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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnEntitySetAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        PowerType<?> powerType = data.get("set");

        if (component == null
                || powerType == null
                || !(component.getPower(powerType) instanceof EntitySetPower entitySetPower)) {
            return;
        }

        Consumer<Pair<Entity, Entity>> biEntityAction = data.get("bientity_action");
        Predicate<Pair<Entity, Entity>> biEntityCondition = data.get("bientity_condition");

        // Snapshot the UUID set so mutations inside the action don't cause ConcurrentModificationException.
        List<UUID> uuids = new LinkedList<>(entitySetPower.getIterationSet());
        if (data.getBoolean("reverse")) {
            Collections.reverse(uuids);
        }

        int limit = data.get("limit");
        if (limit <= 0) limit = Integer.MAX_VALUE;

        int processed = 0;

        for (UUID uuid : uuids) {
            // Resolve the entity; getEntity() will evict destroyed entries and
            // return null for members that no longer exist.
            Entity entityFromSet = entitySetPower.getEntity(uuid);

            // Skip dead / unloaded members rather than crashing or mis-counting.
            if (entityFromSet == null) continue;

            Pair<Entity, Entity> entityPair = new Pair<>(entity, entityFromSet);

            if (biEntityCondition == null || biEntityCondition.test(entityPair)) {
                biEntityAction.accept(entityPair);
                processed++;
            }

            if (processed >= limit) break;
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("action_on_entity_set"),
                new SerializableData()
                        .add("set", ApoliDataTypes.POWER_TYPE)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("limit", SerializableDataTypes.INT, 0)
                        .add("reverse", SerializableDataTypes.BOOLEAN, false),
                ActionOnEntitySetAction::action
        );
    }
}