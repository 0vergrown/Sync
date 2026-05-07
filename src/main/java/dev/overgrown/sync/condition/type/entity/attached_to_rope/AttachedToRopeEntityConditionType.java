package dev.overgrown.sync.condition.type.entity.attached_to_rope;

import dev.overgrown.sync.data.rope.common.RopeFilter;
import dev.overgrown.sync.data.rope.common.RopeManager;
import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class AttachedToRopeEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<AttachedToRopeEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("anchor_type", SerializableDataType.enumValue(RopeFilter.class), RopeFilter.ANY)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
        data -> new AttachedToRopeEntityConditionType(
            data.get("anchor_type"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("anchor_type", conditionType.filter)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final RopeFilter filter;
    private final Comparison comparison;
    private final int compareTo;

    public AttachedToRopeEntityConditionType(RopeFilter filter, Comparison comparison, int compareTo) {
        this.filter = filter;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        if (!(context.entity() instanceof PlayerEntity player)) return false;
        int count = RopeManager.countRopesByOwner(player.getUuid(), filter);
        return comparison.compare(count, compareTo);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.ATTACHED_TO_ROPE;
    }
}
