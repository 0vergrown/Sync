package dev.overgrown.sync.factory.condition.entity;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Set;

public class HasCommandTagCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        Set<String> commandTags = entity.getCommandTags();

        if (data.isPresent("command_tag")) {
            return commandTags.contains(data.getString("command_tag"));
        }

        if (data.isPresent("command_tags")) {
            List<String> requiredTags = data.get("command_tags");
            return commandTags.containsAll(requiredTags);
        }

        // If neither field is present, check if entity has any command tags
        return !commandTags.isEmpty();
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("has_command_tag"),
                new SerializableData()
                        .add("command_tag", SerializableDataTypes.STRING, null)
                        .add("command_tags", SerializableDataTypes.STRINGS, null),
                HasCommandTagCondition::condition
        );
    }
}