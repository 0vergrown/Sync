package dev.overgrown.sync.factory.action.entity;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.utils.Temporary;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class SetSummonMaxLifeAction {
    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof Temporary summonable) {
            final int newMaxLife = data.getInt("amount");
            summonable.setMaxLifetime(newMaxLife);
        } else {
            Sync.LOGGER.warn("Attempted to use set_summon_max_life_ticks action on a non-temporary entity.");
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("set_summon_max_life_ticks"),
                new SerializableData()
                        .add("amount", SerializableDataTypes.INT),
                SetSummonMaxLifeAction::action
        );
    }
}