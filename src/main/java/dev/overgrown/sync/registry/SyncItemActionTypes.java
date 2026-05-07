package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.action.type.item.cooldown.CooldownItemActionType;
import dev.overgrown.sync.registry.utils.ApoliRegistryHelper;
import io.github.apace100.apoli.action.ActionConfiguration;

public class SyncItemActionTypes {

    public static final ActionConfiguration<CooldownItemActionType> COOLDOWN = ApoliRegistryHelper.registerItemAction(
        ActionConfiguration.of(Sync.identifier("cooldown"), CooldownItemActionType.DATA_FACTORY)
    );

    public static void register() {
    }
}
