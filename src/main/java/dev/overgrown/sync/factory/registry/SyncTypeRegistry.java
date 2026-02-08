package dev.overgrown.sync.factory.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.compatibility.aspectslib.SetEntityAspectsPower;
import dev.overgrown.sync.compatibility.aspectslib.condition.HasAspectCondition;
import dev.overgrown.sync.factory.action.bientity.AddToEntitySetAction;
import dev.overgrown.sync.factory.action.bientity.RemoveFromEntitySetAction;
import dev.overgrown.sync.factory.action.block.GhostBlockAction;
import dev.overgrown.sync.factory.action.block.SpawnEntityBlockAction;
import dev.overgrown.sync.factory.action.entity.*;
import dev.overgrown.sync.factory.action.entity.radial_menu.RadialMenuAction;
import dev.overgrown.sync.factory.condition.bientity.InEntitySetCondition;
import dev.overgrown.sync.factory.condition.entity.*;
import dev.overgrown.sync.factory.condition.item.FuelCondition;
import dev.overgrown.sync.factory.power.type.*;
import dev.overgrown.sync.factory.power.type.EntityTextureOverlayPower;
import dev.overgrown.sync.utils.registry.ApoliRegistryHelper;

public class SyncTypeRegistry {

    public static void register() {
        // ========== POWER TYPE REGISTRATIONS ==========
        ApoliRegistryHelper.registerPowerFactory(ActionOnDeathPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(CustomProjectilePower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(EdibleItemPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(EmissivePower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(EntitySetPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(EntityTextureOverlayPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(FlipModelPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(MobsIgnorePower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(ModifyEnchantmentLevelPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(ModifyModelPartsPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(PosePower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(SprintingPower.getFactory());

        // ========== ENTITY ACTION REGISTRATIONS ==========
        ApoliRegistryHelper.registerEntityAction(RadialMenuAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(ActionOnEntitySetAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(CustomProjectileAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(GrantAllPowersAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(PrintAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(RandomTeleportAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(RemovePowerAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(RevokeAllPowersAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(SaveLocationAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(SetSummonMaxLifeAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(SummonCloneAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(SummonMinionAction.getFactory());
        ApoliRegistryHelper.registerEntityAction(TeleportToLocationAction.getFactory());

        // ========== BLOCK ACTION REGISTRATIONS ==========
        ApoliRegistryHelper.registerBlockAction(SpawnEntityBlockAction.getFactory());
        ApoliRegistryHelper.registerBlockAction(GhostBlockAction.getFactory());

        // ========== ENTITY CONDITION REGISTRATIONS ==========
        ApoliRegistryHelper.registerEntityCondition(EntityInRadiusCondition.getFactory());
        ApoliRegistryHelper.registerEntityCondition(EntitySetSizeCondition.getFactory());
        ApoliRegistryHelper.registerEntityCondition(HasCommandTagCondition.getFactory());
        ApoliRegistryHelper.registerEntityCondition(InPoseCondition.getFactory());
        ApoliRegistryHelper.registerEntityCondition(KeyPressedCondition.getFactory());
        ApoliRegistryHelper.registerEntityCondition(PlayerModelTypeCondition.getFactory());

        // ========== BIENTITY ACTION REGISTRATIONS ==========
        ApoliRegistryHelper.registerBientityAction(AddToEntitySetAction.getFactory());
        ApoliRegistryHelper.registerBientityAction(RemoveFromEntitySetAction.getFactory());

        // ========== BIENTITY CONDITION REGISTRATIONS ==========
        ApoliRegistryHelper.registerBientityCondition(InEntitySetCondition.getFactory());

        // ========== ITEM CONDITION REGISTRATIONS ==========
        ApoliRegistryHelper.registerItemCondition(FuelCondition.getFactory());

        if (Sync.HAS_ASPECTSLIB) {
            ApoliRegistryHelper.registerPowerFactory(SetEntityAspectsPower.createFactory());
            ApoliRegistryHelper.registerEntityCondition(HasAspectCondition.getFactory());
        }
    }
}