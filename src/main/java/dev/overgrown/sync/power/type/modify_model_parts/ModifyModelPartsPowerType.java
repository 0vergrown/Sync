package dev.overgrown.sync.power.type.modify_model_parts;

import dev.overgrown.sync.power.type.modify_model_parts.util.ModelPartTransformation;
import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyModelPartsPowerType extends PowerType {

    public static final TypedDataObjectFactory<ModifyModelPartsPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("transformations", ModelPartTransformation.MODEL_PART_TRANSFORMATIONS),
        (data, condition) -> new ModifyModelPartsPowerType(data.get("transformations"), condition),
        (powerType, sd) -> sd.instance().set("transformations", powerType.transformations)
    );

    private final List<ModelPartTransformation> transformations;

    public ModifyModelPartsPowerType(List<ModelPartTransformation> transformations, Optional<EntityCondition> condition) {
        super(condition);
        this.transformations = transformations;
    }

    public List<ModelPartTransformation> getTransformations() {
        return transformations;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.MODIFY_MODEL_PARTS;
    }
}
