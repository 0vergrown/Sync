package dev.overgrown.sync.factory.condition.entity.disguised;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.disguise.DisguiseManager;
import dev.overgrown.sync.factory.data.disguise.client.ClientDisguiseManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;

public class DisguisedEntityCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        // Server-side check
        if (!entity.getWorld().isClient()) {
            return DisguiseManager.isDisguised(entity.getUuid());
        }
        // Client-side: use client manager, but only if we're actually on the client.
        // The class will only be loaded when this code path executes on a client.
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return ClientDisguiseManager.isDisguised(entity.getId());
        }
        // Fallback (should never happen)
        return false;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("disguised"),
                new SerializableData(),
                DisguisedEntityCondition::condition
        );
    }
}