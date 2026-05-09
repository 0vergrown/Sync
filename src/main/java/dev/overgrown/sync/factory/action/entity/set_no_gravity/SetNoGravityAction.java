package dev.overgrown.sync.factory.action.entity.set_no_gravity;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class SetNoGravityAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        // If value is present, use it; otherwise toggle the current state
        if (data.isPresent("value")) {
            boolean value = data.getBoolean("value");
            entity.setNoGravity(value);
        } else {
            // Toggle the current value
            entity.setNoGravity(!entity.hasNoGravity());
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("set_no_gravity"),
                new SerializableData()
                        .add("value", SerializableDataTypes.BOOLEAN, null),
                SetNoGravityAction::action
        );
    }
}