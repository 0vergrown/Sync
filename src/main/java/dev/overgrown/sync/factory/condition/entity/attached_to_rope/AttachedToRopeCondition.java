package dev.overgrown.sync.factory.condition.entity.attached_to_rope;

import dev.overgrown.sync.factory.data.rope.common.RopeFilter;
import dev.overgrown.sync.factory.data.rope.common.RopeManager;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class AttachedToRopeCondition {

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                new Identifier("sync", "attached_to_rope"),
                new SerializableData()
                        .add("anchor_type", SerializableDataType.enumValue(RopeFilter.class), RopeFilter.ANY)
                        .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                        .add("compare_to", SerializableDataTypes.INT, 1),
                (data, entity) -> {
                    if (!(entity instanceof PlayerEntity player)) return false;
                    RopeFilter filter = data.get("anchor_type");
                    Comparison comparison = data.get("comparison");
                    int compareTo = data.get("compare_to");
                    int count = RopeManager.countRopesByOwner(player.getUuid(), filter);
                    return comparison.compare(count, compareTo);
                }
        );
    }
}
