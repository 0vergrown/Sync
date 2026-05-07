package dev.overgrown.sync.action.type.entity.save_location;

import dev.overgrown.sync.data.teleportation.EntityLocationsState;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public class SaveLocationEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SaveLocationEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("id", SerializableDataTypes.STRING)
            .add("overwrite", SerializableDataTypes.BOOLEAN, true),
        data -> new SaveLocationEntityActionType(
            data.get("id"),
            data.get("overwrite")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("id", actionType.id)
            .set("overwrite", actionType.overwrite)
    );

    private final String id;
    private final boolean overwrite;

    public SaveLocationEntityActionType(String id, boolean overwrite) {
        this.id = id;
        this.overwrite = overwrite;
    }

    @Override
    public void accept(EntityActionContext context) {
        Entity entity = context.entity();
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;

        EntityLocationsState state = EntityLocationsState.get(serverWorld);
        state.saveLocation(
            entity.getUuid(),
            id,
            entity.getPos(),
            entity.getWorld().getRegistryKey(),
            entity.getYaw(),
            entity.getPitch(),
            overwrite
        );
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.SAVE_LOCATION;
    }
}
