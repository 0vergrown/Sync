package dev.overgrown.sync.action.type.entity.toggle_transfer_mode;

import dev.overgrown.sync.data.transfer.TransferModeManager;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import org.jetbrains.annotations.NotNull;

public class ToggleTransferModeEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ToggleTransferModeEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData(),
        data -> new ToggleTransferModeEntityActionType(),
        (actionType, serializableData) -> serializableData.instance()
    );

    @Override
    public void accept(EntityActionContext context) {
        if (context.entity().getWorld().isClient()) return;
        TransferModeManager.toggle(context.entity());
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.TOGGLE_TRANSFER_MODE;
    }
}
