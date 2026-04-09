package dev.overgrown.sync.factory.power.type.modify_model_parts;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.power.type.modify_model_parts.utils.ModelPartTransformation;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class ModifyModelPartsPower extends Power {
    private final List<ModelPartTransformation> transformations;

    public static PowerFactory<ModifyModelPartsPower> getFactory() {
        return new PowerFactory<ModifyModelPartsPower>(
                Sync.identifier("modify_model_parts"),
                new SerializableData()
                        .add("transformations", ModelPartTransformation.MODEL_PART_TRANSFORMATIONS),
                data ->
                        (type, player) ->
                                new ModifyModelPartsPower(type, player, data.get("transformations")))
                .allowCondition();
    }

    public ModifyModelPartsPower(PowerType<?> type, LivingEntity entity, List<ModelPartTransformation> transformations) {
        super(type, entity);
        this.transformations = transformations;
    }

    public List<ModelPartTransformation> getTransformations() {
        return transformations;
    }
}