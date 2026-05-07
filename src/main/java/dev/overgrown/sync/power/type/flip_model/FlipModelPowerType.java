package dev.overgrown.sync.power.type.flip_model;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FlipModelPowerType extends PowerType {

    public static final TypedDataObjectFactory<FlipModelPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("flip_view", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new FlipModelPowerType(
            data.get("flip_view"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("flip_view", powerType.flipView)
    );

    private final boolean flipView;

    public FlipModelPowerType(boolean flipView, Optional<EntityCondition> condition) {
        super(condition);
        this.flipView = flipView;
    }

    public boolean shouldFlipView() {
        return flipView;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.FLIP_MODEL;
    }
}
