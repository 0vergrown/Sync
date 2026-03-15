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
        if (entity == null) return false;

        // Server-side: check the authoritative disguise registry.
        if (!entity.getWorld().isClient()) {
            return DisguiseManager.isDisguised(entity.getUuid());
        }

        // Client-side: delegate to the client-side mirror.
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return ClientDisguiseManager.isDisguised(entity.getId());
        }

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