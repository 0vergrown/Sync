package dev.overgrown.sync.factory.condition.entity.is_selected_stolen_power;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.bientity.transfer.StolenPowerSlotManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class IsSelectedStolenPowerCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        Identifier source = data.isPresent("source") ? data.get("source") : null;
        if (source != null) {
            return StolenPowerSlotManager.isSourceSelected(entity, source);
        }
        return StolenPowerSlotManager.getSelectedSource(entity) != null;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("is_selected_stolen_power"),
                new SerializableData()
                        .add("source", SerializableDataTypes.IDENTIFIER, null),
                IsSelectedStolenPowerCondition::condition
        );
    }
}
