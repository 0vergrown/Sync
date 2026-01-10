package dev.overgrown.sync.utils;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.List;

public class ModelPartTransformation {
    private String modelPart, type;
    private Float value;

    public ModelPartTransformation(String modelPart, String type, Float value) {
        this.modelPart = modelPart;
        this.type = type;
        this.value = value;
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

    public static final SerializableDataType<ModelPartTransformation> MODEL_PART_TRANSFORMATION = SerializableDataType.compound(
            ModelPartTransformation.class,
            new SerializableData()
                    .add("model_part", SerializableDataTypes.STRING)
                    .add("type", SerializableDataTypes.STRING)
                    .add("value", SerializableDataTypes.FLOAT),
            data -> new ModelPartTransformation(
                    data.get("model_part"),
                    data.get("type"),
                    data.get("value")
            ),
            (data, inst) -> {
                SerializableData.Instance dataInst = data.new Instance();
                dataInst.set("model_part", inst.getModelPart());
                dataInst.set("type", inst.getType());
                dataInst.set("value", inst.getValue());
                return dataInst;
            });

    public static final SerializableDataType<List<ModelPartTransformation>> MODEL_PART_TRANSFORMATIONS =
            SerializableDataType.list(MODEL_PART_TRANSFORMATION);
}