package dev.overgrown.sync.factory.condition.entity.key_pressed;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class KeyPressedCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        String key = data.getString("key");
        boolean continuous = data.getBoolean("continuous");
        return KeyPressManager.getKeyState(entity.getUuid(), key, continuous);
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("key_pressed"),
                new SerializableData()
                        .add("key", SerializableDataTypes.STRING)
                        .add("continuous", SerializableDataTypes.BOOLEAN, true),
                KeyPressedCondition::condition
        );
    }
}