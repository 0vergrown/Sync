package dev.overgrown.sync.factory.condition.item.holder;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class HolderCondition {

    @SuppressWarnings("unchecked")
    public static ConditionFactory<ItemStack> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("holder_condition"),
                new SerializableData()
                        .add("condition", ApoliDataTypes.ENTITY_CONDITION, null),
                (data, stack) -> {
                    if (stack.isEmpty()) return false;
                    Entity holder = ((EntityLinkedItemStack)(Object) stack).getEntity();
                    if (holder == null) return false;
                    if (!data.isPresent("condition")) return true;
                    return ((ConditionFactory<Entity>.Instance) data.get("condition")).test(holder);
                }
        );
    }
}