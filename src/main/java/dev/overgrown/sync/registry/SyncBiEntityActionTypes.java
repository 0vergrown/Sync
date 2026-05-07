package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.action.type.bientity.convert_entity.ConvertEntityBiEntityActionType;
import dev.overgrown.sync.action.type.bientity.disguise.DisguiseBiEntityActionType;
import dev.overgrown.sync.action.type.bientity.execute_command.ExecuteCommandBiEntityActionType;
import dev.overgrown.sync.action.type.bientity.explode.ExplodeBiEntityActionType;
import dev.overgrown.sync.action.type.bientity.liberate_power.LiberatePowerBiEntityActionType;
import dev.overgrown.sync.action.type.bientity.rope_leash.RopeLeashBiEntityActionType;
import dev.overgrown.sync.action.type.bientity.suppress_power.SuppressPowerBiEntityActionType;
import dev.overgrown.sync.action.type.bientity.transfer.TransferBiEntityActionType;
import dev.overgrown.sync.registry.utils.ApoliRegistryHelper;
import io.github.apace100.apoli.action.ActionConfiguration;

public class SyncBiEntityActionTypes {

    public static final ActionConfiguration<ConvertEntityBiEntityActionType> CONVERT_ENTITY = ApoliRegistryHelper.registerBientityAction(
        ActionConfiguration.of(Sync.identifier("convert_entity"), ConvertEntityBiEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<DisguiseBiEntityActionType> DISGUISE = ApoliRegistryHelper.registerBientityAction(
        ActionConfiguration.of(Sync.identifier("disguise"), DisguiseBiEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<ExecuteCommandBiEntityActionType> EXECUTE_COMMAND = ApoliRegistryHelper.registerBientityAction(
        ActionConfiguration.of(Sync.identifier("execute_command"), ExecuteCommandBiEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<ExplodeBiEntityActionType> EXPLODE = ApoliRegistryHelper.registerBientityAction(
        ActionConfiguration.of(Sync.identifier("explode"), ExplodeBiEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<LiberatePowerBiEntityActionType> LIBERATE_POWER = ApoliRegistryHelper.registerBientityAction(
        ActionConfiguration.of(Sync.identifier("liberate_power"), LiberatePowerBiEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<RopeLeashBiEntityActionType> ROPE_LEASH = ApoliRegistryHelper.registerBientityAction(
        ActionConfiguration.of(Sync.identifier("rope_leash"), RopeLeashBiEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<SuppressPowerBiEntityActionType> SUPPRESS_POWER = ApoliRegistryHelper.registerBientityAction(
        ActionConfiguration.of(Sync.identifier("suppress_power"), SuppressPowerBiEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<TransferBiEntityActionType> TRANSFER = ApoliRegistryHelper.registerBientityAction(
        ActionConfiguration.of(Sync.identifier("transfer"), TransferBiEntityActionType.DATA_FACTORY)
    );

    public static void register() {
    }
}
