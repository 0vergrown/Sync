package dev.overgrown.sync.factory.condition.entity.key_pressed;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class KeyPressedCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }

        Active.Key activeKey = data.get("key");
        return KeyPressManager.getKeyState(entity.getUuid(), activeKey.key, activeKey.continuous);
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("key_pressed"),
                new SerializableData()
                        .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY),
                KeyPressedCondition::condition
        );
    }
}