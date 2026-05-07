package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.condition.type.item.holder.HolderItemConditionType;
import dev.overgrown.sync.condition.type.item.mod_loaded.ModLoadedItemConditionType;
import dev.overgrown.sync.condition.type.item.on_cooldown.OnCooldownItemConditionType;
import dev.overgrown.sync.registry.utils.ApoliRegistryHelper;
import io.github.apace100.apoli.condition.ConditionConfiguration;

public class SyncItemConditionTypes {

    public static final ConditionConfiguration<HolderItemConditionType> HOLDER = ApoliRegistryHelper.registerItemCondition(
        ConditionConfiguration.of(Sync.identifier("holder_condition"), HolderItemConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<ModLoadedItemConditionType> MOD_LOADED = ApoliRegistryHelper.registerItemCondition(
        ConditionConfiguration.of(Sync.identifier("mod_loaded"), ModLoadedItemConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<OnCooldownItemConditionType> ON_COOLDOWN = ApoliRegistryHelper.registerItemCondition(
        ConditionConfiguration.of(Sync.identifier("on_cooldown"), OnCooldownItemConditionType.DATA_FACTORY)
    );

    public static void register() {
    }
}
