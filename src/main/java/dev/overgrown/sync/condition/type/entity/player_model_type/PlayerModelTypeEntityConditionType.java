package dev.overgrown.sync.condition.type.entity.player_model_type;

import dev.overgrown.sync.condition.type.entity.player_model_type.util.PlayerModelTypeManager;
import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PlayerModelTypeEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<PlayerModelTypeEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("model_type", SerializableDataTypes.STRING),
        data -> new PlayerModelTypeEntityConditionType(data.get("model_type")),
        (conditionType, serializableData) -> serializableData.instance()
            .set("model_type", conditionType.modelType)
    );

    private final String modelType;

    public PlayerModelTypeEntityConditionType(String modelType) {
        this.modelType = modelType;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        if (!(context.entity() instanceof ServerPlayerEntity player)) {
            return false;
        }
        return modelType.equalsIgnoreCase(PlayerModelTypeManager.getModelType(player));
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.PLAYER_MODEL_TYPE;
    }
}
