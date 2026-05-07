package dev.overgrown.sync.action.type.entity.remove_disguise;

import dev.overgrown.sync.data.disguise.DisguiseManager;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class RemoveDisguiseEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<RemoveDisguiseEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData(),
        data -> new RemoveDisguiseEntityActionType(),
        (actionType, serializableData) -> serializableData.instance()
    );

    @Override
    public void accept(EntityActionContext context) {
        if (context.entity() instanceof LivingEntity living) {
            DisguiseManager.removeDisguise(living);
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.REMOVE_DISGUISE;
    }
}
