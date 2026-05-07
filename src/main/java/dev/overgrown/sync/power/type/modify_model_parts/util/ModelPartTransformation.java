package dev.overgrown.sync.power.type.modify_model_parts.util;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.List;

public class ModelPartTransformation {

    private final String modelPart;
    private final String type;
    private final float value;
    private final boolean overrideAnimation;

    public ModelPartTransformation(String modelPart, String type, float value, boolean overrideAnimation) {
        this.modelPart = modelPart;
        this.type = type;
        this.value = value;
        this.overrideAnimation = overrideAnimation;
    }

    public String getModelPart() { return modelPart; }
    public String getType() { return type; }
    public float getValue() { return value; }
    public boolean getOverrideAnimation() { return overrideAnimation; }

    public static final SerializableDataType<ModelPartTransformation> MODEL_PART_TRANSFORMATION = SerializableDataType.compound(
        new SerializableData()
            .add("model_part", SerializableDataTypes.STRING)
            .add("type", SerializableDataTypes.STRING)
            .add("value", SerializableDataTypes.FLOAT)
            .add("override_animation", SerializableDataTypes.BOOLEAN, false),
        data -> new ModelPartTransformation(
            data.get("model_part"),
            data.get("type"),
            data.get("value"),
            data.get("override_animation")
        ),
        (inst, data) -> data.instance()
            .set("model_part", inst.getModelPart())
            .set("type", inst.getType())
            .set("value", inst.getValue())
            .set("override_animation", inst.getOverrideAnimation())
    );

    public static final SerializableDataType<List<ModelPartTransformation>> MODEL_PART_TRANSFORMATIONS = MODEL_PART_TRANSFORMATION.list();
}
