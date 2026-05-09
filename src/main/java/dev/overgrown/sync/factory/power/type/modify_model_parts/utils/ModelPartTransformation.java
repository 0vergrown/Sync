package dev.overgrown.sync.factory.power.type.modify_model_parts.utils;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.List;

public class ModelPartTransformation {
    private String modelPart, type;
    private Float value;
    private Boolean overrideAnimation;

    public ModelPartTransformation(String modelPart, String type, Float value, Boolean overrideAnimation) {
        this.modelPart = modelPart;
        this.type = type;
        this.value = value;
        this.overrideAnimation = overrideAnimation;
    }

    public String getModelPart() {
        return this.modelPart;
    }

    public String getType() {
        return this.type;
    }

    public Float getValue() {
        return this.value;
    }

    public Boolean getOverrideAnimation() {
        return this.overrideAnimation;
    }

    public static final SerializableDataType<ModelPartTransformation> MODEL_PART_TRANSFORMATION = SerializableDataType.compound(
            ModelPartTransformation.class,
            new SerializableData()
                    .add("model_part", SerializableDataTypes.STRING)
                    .add("type", SerializableDataTypes.STRING)
                    .add("value", SerializableDataTypes.FLOAT)
                    .add("override_animation", SerializableDataTypes.BOOLEAN, false), // Default to false
            data -> new ModelPartTransformation(
                    data.get("model_part"),
                    data.get("type"),
                    data.get("value"),
                    data.get("override_animation")
            ),
            (data, inst) -> {
                SerializableData.Instance dataInst = data.new Instance();
                dataInst.set("model_part", inst.getModelPart());
                dataInst.set("type", inst.getType());
                dataInst.set("value", inst.getValue());
                dataInst.set("override_animation", inst.getOverrideAnimation());
                return dataInst;
            });

    public static final SerializableDataType<List<ModelPartTransformation>> MODEL_PART_TRANSFORMATIONS =
            SerializableDataType.list(MODEL_PART_TRANSFORMATION);
}