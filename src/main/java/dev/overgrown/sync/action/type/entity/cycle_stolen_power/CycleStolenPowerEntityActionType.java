package dev.overgrown.sync.action.type.entity.cycle_stolen_power;

import dev.overgrown.sync.data.transfer.StolenPowerSlotManager;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

public class CycleStolenPowerEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<CycleStolenPowerEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("delta", SerializableDataTypes.INT, 1),
        data -> new CycleStolenPowerEntityActionType(data.get("delta")),
        (actionType, serializableData) -> serializableData.instance()
            .set("delta", actionType.delta)
    );

    private final int delta;

    public CycleStolenPowerEntityActionType(int delta) {
        this.delta = delta;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (context.entity().getWorld().isClient()) return;
        StolenPowerSlotManager.cycle(context.entity(), delta);
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.CYCLE_STOLEN_POWER;
    }
}
