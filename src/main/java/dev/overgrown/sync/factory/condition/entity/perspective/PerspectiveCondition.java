package dev.overgrown.sync.factory.condition.entity.perspective;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.condition.entity.perspective.utils.PerspectiveManager;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class PerspectiveCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            // Only meaningful for players; non-players never match.
            return false;
        }

        if (entity.getWorld().isClient()) {
            return false;
        }

        String current = PerspectiveManager.getPerspective(player);
        List<String> allowed = data.get("perspectives");
        return allowed.contains(current);
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("perspective"),
                new SerializableData()
                        .add("perspectives", SerializableDataTypes.STRINGS),
                PerspectiveCondition::condition
        );
    }
}