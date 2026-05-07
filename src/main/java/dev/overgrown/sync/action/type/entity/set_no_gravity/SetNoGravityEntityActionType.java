package dev.overgrown.sync.action.type.entity.set_no_gravity;

import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SetNoGravityEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SetNoGravityEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN.optional(), Optional.empty()),
        data -> new SetNoGravityEntityActionType(data.get("value")),
        (actionType, serializableData) -> serializableData.instance()
            .set("value", actionType.value)
    );

    private final Optional<Boolean> value;

    public SetNoGravityEntityActionType(Optional<Boolean> value) {
        this.value = value;
    }

    @Override
    public void accept(EntityActionContext context) {
        Entity entity = context.entity();
        if (value.isPresent()) {
            entity.setNoGravity(value.get());
        } else {
            entity.setNoGravity(!entity.hasNoGravity());
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.SET_NO_GRAVITY;
    }
}
