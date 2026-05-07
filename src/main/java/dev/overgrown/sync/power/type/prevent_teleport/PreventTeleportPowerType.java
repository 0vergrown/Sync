package dev.overgrown.sync.power.type.prevent_teleport;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventTeleportPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventTeleportPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventTeleportPowerType(
            data.get("entity_action"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
    );

    private final Optional<EntityAction> entityAction;

    public PreventTeleportPowerType(Optional<EntityAction> entityAction, Optional<EntityCondition> condition) {
        super(condition);
        this.entityAction = entityAction;
    }

    public void onTeleportPrevented() {
        entityAction.ifPresent(action -> action.execute(getHolder()));
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.PREVENT_TELEPORT;
    }
}
