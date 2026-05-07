package dev.overgrown.sync.condition.type.item.holder;

import dev.overgrown.sync.registry.SyncItemConditionTypes;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HolderItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<HolderItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("condition", EntityCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new HolderItemConditionType(data.get("condition")),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.condition)
    );

    private final Optional<EntityCondition> condition;

    public HolderItemConditionType(Optional<EntityCondition> condition) {
        this.condition = condition;
    }

    @Override
    public boolean test(ItemConditionContext context) {
        if (context.stack().isEmpty()) return false;
        Entity holder = ((EntityLinkedItemStack) (Object) context.stack()).apoli$getEntity();
        if (holder == null) return false;
        return condition.map(c -> c.test(holder)).orElse(true);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncItemConditionTypes.HOLDER;
    }
}
