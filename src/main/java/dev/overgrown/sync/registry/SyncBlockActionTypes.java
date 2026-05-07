package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.action.type.block.ghost_block.GhostBlockActionType;
import dev.overgrown.sync.registry.utils.ApoliRegistryHelper;
import io.github.apace100.apoli.action.ActionConfiguration;

public class SyncBlockActionTypes {

    public static final ActionConfiguration<GhostBlockActionType> GHOST_BLOCK = ApoliRegistryHelper.registerBlockAction(
        ActionConfiguration.of(Sync.identifier("ghost_block"), GhostBlockActionType.DATA_FACTORY)
    );

    public static void register() {
    }
}
