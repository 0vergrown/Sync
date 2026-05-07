package dev.overgrown.sync.condition.type.entity.disguised;

import dev.overgrown.sync.data.disguise.DisguiseManager;
import dev.overgrown.sync.data.disguise.client.ClientDisguiseManager;
import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class DisguisedEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<DisguisedEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData(),
        data -> new DisguisedEntityConditionType(),
        (conditionType, serializableData) -> serializableData.instance()
    );

    @Override
    public boolean test(EntityConditionContext context) {
        Entity entity = context.entity();
        if (entity == null) return false;

        if (!entity.getWorld().isClient()) {
            return DisguiseManager.isDisguised(entity.getUuid());
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return ClientDisguiseManager.isDisguised(entity.getId());
        }

        return false;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.DISGUISED;
    }
}
