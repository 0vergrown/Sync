package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.condition.type.bientity.colliding.CollidingBiEntityConditionType;
import dev.overgrown.sync.condition.type.bientity.command.CommandBiEntityConditionType;
import dev.overgrown.sync.condition.type.bientity.disguised.DisguisedBiEntityConditionType;
import dev.overgrown.sync.registry.utils.ApoliRegistryHelper;
import io.github.apace100.apoli.condition.ConditionConfiguration;

public class SyncBiEntityConditionTypes {

    public static final ConditionConfiguration<CollidingBiEntityConditionType> COLLIDING = ApoliRegistryHelper.registerBientityCondition(
        ConditionConfiguration.of(Sync.identifier("colliding"), CollidingBiEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<CommandBiEntityConditionType> COMMAND = ApoliRegistryHelper.registerBientityCondition(
        ConditionConfiguration.of(Sync.identifier("command"), CommandBiEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<DisguisedBiEntityConditionType> DISGUISED = ApoliRegistryHelper.registerBientityCondition(
        ConditionConfiguration.of(Sync.identifier("disguised"), DisguisedBiEntityConditionType.DATA_FACTORY)
    );

    public static void register() {
    }
}
