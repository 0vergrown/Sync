package dev.overgrown.sync.factory.power.type.pose;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.Prioritized;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class PosePower extends Power implements Prioritized<PosePower> {

    public static final SerializableDataType<EntityPose> ENTITY_POSE =
            SerializableDataType.enumValue(EntityPose.class);
    public static final SerializableDataType<BipedEntityModel.ArmPose> ARM_POSE =
            SerializableDataType.enumValue(BipedEntityModel.ArmPose.class);

    @Nullable private final EntityPose entityPose;
    @Nullable private final BipedEntityModel.ArmPose armPose;
    private final int priority;

    public PosePower(PowerType<?> type, LivingEntity entity,
                     @Nullable EntityPose entityPose,
                     @Nullable BipedEntityModel.ArmPose armPose,
                     int priority) {
        super(type, entity);
        this.entityPose = entityPose;
        this.armPose = armPose;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Nullable
    public EntityPose getEntityPose() {
        return entityPose;
    }

    @Nullable
    public BipedEntityModel.ArmPose getArmPose() {
        return armPose;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("pose"),
                new SerializableData()
                        .add("entity_pose", ENTITY_POSE, null)
                        .add("arm_pose", ARM_POSE, null)
                        .add("priority", SerializableDataTypes.INT, 0),
                data -> (powerType, entity) -> new PosePower(
                        powerType,
                        entity,
                        data.get("entity_pose"),
                        data.get("arm_pose"),
                        data.get("priority")
                )
        ).allowCondition();
    }
}