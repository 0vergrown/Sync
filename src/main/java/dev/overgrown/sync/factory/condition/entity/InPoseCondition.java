package dev.overgrown.sync.factory.condition.entity;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.power.type.PosePower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class InPoseCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return entity.isInPose(data.get("pose"));
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("in_pose"),
                new SerializableData()
                        .add("pose", PosePower.ENTITY_POSE),
                InPoseCondition::condition
        );
    }

}