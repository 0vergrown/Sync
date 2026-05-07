package dev.overgrown.sync.condition.type.entity.in_pose;

import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.EntityPose;
import org.jetbrains.annotations.NotNull;

public class InPoseEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<InPoseEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("pose", ApoliDataTypes.ENTITY_POSE),
        data -> new InPoseEntityConditionType(data.get("pose")),
        (conditionType, serializableData) -> serializableData.instance()
            .set("pose", conditionType.pose)
    );

    private final EntityPose pose;

    public InPoseEntityConditionType(EntityPose pose) {
        this.pose = pose;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        return context.entity().isInPose(pose);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.IN_POSE;
    }
}
