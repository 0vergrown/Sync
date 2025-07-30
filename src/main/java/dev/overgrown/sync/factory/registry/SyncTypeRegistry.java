package dev.overgrown.sync.factory.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.compatibility.aspectslib.SetEntityAspectsPower;
import dev.overgrown.sync.compatibility.aspectslib.condition.HasAspectCondition;
import dev.overgrown.sync.factory.action.bientity.AddToEntitySetAction;
import dev.overgrown.sync.factory.action.bientity.RemoveFromEntitySetAction;
import dev.overgrown.sync.factory.action.entity.ActionOnEntitySetAction;
import dev.overgrown.sync.factory.action.entity.RandomTeleportAction;
import dev.overgrown.sync.factory.condition.entity.EntityInRadiusCondition;
import dev.overgrown.sync.factory.condition.entity.EntitySetSizeCondition;
import dev.overgrown.sync.factory.condition.entity.InEntitySetCondition;
import dev.overgrown.sync.factory.condition.entity.InPoseCondition;
import dev.overgrown.sync.factory.power.type.EntitySetPower;
import dev.overgrown.sync.factory.power.type.ModelFlipPower;
import dev.overgrown.sync.factory.power.type.PosePower;
import dev.overgrown.sync.utils.ApoliRegistryHelper;

public class SyncTypeRegistry {

    public static void register() {

        // ========== POWER TYPE REGISTRATIONS ==========
        ApoliRegistryHelper.registerPowerFactory(EntitySetPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(ModelFlipPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(PosePower.getFactory());

        // ========== ENTITY ACTION REGISTRATIONS ==========
        ApoliRegistryHelper.registerEntityAction(ActionOnEntitySetAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(RandomTeleportAction.getFactory());

        // ========== ENTITY CONDITION REGISTRATIONS ==========
        ApoliRegistryHelper.registerEntityCondition(EntitySetSizeCondition.getFactory());
        ApoliRegistryHelper.registerEntityCondition(InPoseCondition.getFactory());
        ApoliRegistryHelper.registerEntityCondition(EntityInRadiusCondition.getFactory());

        // ========== BIENTITY ACTION REGISTRATIONS ==========
        ApoliRegistryHelper.registerBientityAction(AddToEntitySetAction.getFactory());
        ApoliRegistryHelper.registerBientityAction(RemoveFromEntitySetAction.getFactory());

        // ========== BIENTITY CONDITION REGISTRATIONS ==========
        ApoliRegistryHelper.registerBientityCondition(InEntitySetCondition.getFactory());

        if (Sync.HAS_ASPECTSLIB) {
            ApoliRegistryHelper.registerPowerFactory(SetEntityAspectsPower.createFactory());
            ApoliRegistryHelper.registerEntityCondition(HasAspectCondition.getFactory());
        }
    }
}