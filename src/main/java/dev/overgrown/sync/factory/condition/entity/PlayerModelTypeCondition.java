package dev.overgrown.sync.factory.condition.entity;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.utils.player_model_type.PlayerModelTypeManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerModelTypeCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return false;
        }

        String expectedModelType = data.getString("model_type");
        String actualModelType = PlayerModelTypeManager.getModelType(player);

        return expectedModelType.equalsIgnoreCase(actualModelType);
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("player_model_type"),
                new SerializableData()
                        .add("model_type", SerializableDataTypes.STRING),
                PlayerModelTypeCondition::condition
        );
    }
}