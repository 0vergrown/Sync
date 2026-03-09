package dev.overgrown.sync.factory.condition.entity.is_selected_stolen_power;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.bientity.transfer.StolenPowerSlotManager;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class IsSelectedStolenPowerCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        PowerType<?> powerType     = data.get("power");
        Identifier   transferSource = data.get("source");

        return StolenPowerSlotManager.isPowerInSelectedPackage(entity, powerType, transferSource);
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("is_selected_stolen_power"),
                new SerializableData()
                        .add("power", ApoliDataTypes.POWER_TYPE, null)
                        .add("source", SerializableDataTypes.IDENTIFIER,
                                StolenPowerSlotManager.DEFAULT_SOURCE),
                IsSelectedStolenPowerCondition::condition
        );
    }
}