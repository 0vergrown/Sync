package dev.overgrown.sync.factory.condition.item.mod_loaded;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.condition.entity.mod_loaded.ModLoadedCondition;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.item.ItemStack;

public class ModLoadedItemCondition {

    public static ConditionFactory<ItemStack> getFactory() {
        return new ConditionFactory<>(
                Sync.identifier("mod_loaded"),
                ModLoadedCondition.dataSchema(),
                (data, stack) -> ModLoadedCondition.check(data)
        );
    }
}
