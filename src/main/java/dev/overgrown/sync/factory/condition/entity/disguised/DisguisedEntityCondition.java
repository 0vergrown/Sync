package dev.overgrown.sync.factory.condition.entity.disguised;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.disguise.DisguiseManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class DisguisedEntityCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return DisguiseManager.isDisguised(entity.getUuid());
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("disguised"),
                new SerializableData(),
                DisguisedEntityCondition::condition
        );
    }
}