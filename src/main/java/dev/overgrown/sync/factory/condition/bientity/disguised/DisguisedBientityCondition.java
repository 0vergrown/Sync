package dev.overgrown.sync.factory.condition.bientity.disguised;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.disguise.DisguiseManager;
import dev.overgrown.sync.factory.disguise.client.ClientDisguiseManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class DisguisedBientityCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor = pair.getLeft();
        Entity target = pair.getRight();

        // Server-side check
        if (!actor.getWorld().isClient()) {
            return DisguiseManager.isDisguisedAs(actor.getUuid(), target);
        }

        // Client-side: only if we're on the client
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return ClientDisguiseManager.isDisguisedAs(actor.getId(), target);
        }

        return false;
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("disguised"),
                new SerializableData(),
                DisguisedBientityCondition::condition
        );
    }
}