package dev.overgrown.sync.power.type.modify_player_model;

import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ModifyPlayerModelPowerType extends PowerType {

    public static final TypedDataObjectFactory<ModifyPlayerModelPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("model", SerializableDataType.enumValue(Model.class)),
        (data, condition) -> new ModifyPlayerModelPowerType(
            data.get("model"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("model", powerType.model)
    );

    public final Model model;

    public ModifyPlayerModelPowerType(Model model, Optional<EntityCondition> condition) {
        super(condition);
        this.model = model;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.MODIFY_PLAYER_MODEL;
    }

    public enum Model {
        FOUR_ARMS,
        STINKFLY
    }
}
