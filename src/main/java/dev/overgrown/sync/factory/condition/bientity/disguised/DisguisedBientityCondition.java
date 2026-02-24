package dev.overgrown.sync.factory.condition.bientity.disguised;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.disguise.DisguiseManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class DisguisedBientityCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor  = pair.getLeft();
        Entity target = pair.getRight();
        return DisguiseManager.isDisguisedAs(actor.getUuid(), target);
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("disguised"),
                new SerializableData(),
                DisguisedBientityCondition::condition
        );
    }
}