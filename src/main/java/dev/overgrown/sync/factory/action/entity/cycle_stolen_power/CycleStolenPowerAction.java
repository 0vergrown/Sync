package dev.overgrown.sync.factory.action.entity.cycle_stolen_power;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.bientity.transfer.StolenPowerSlotManager;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class CycleStolenPowerAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity.getWorld().isClient()) return;
        int delta = data.getInt("delta");
        StolenPowerSlotManager.cycle(entity, delta);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("cycle_stolen_power"),
                new SerializableData()
                        .add("delta", SerializableDataTypes.INT, 1),
                CycleStolenPowerAction::action
        );
    }
}
