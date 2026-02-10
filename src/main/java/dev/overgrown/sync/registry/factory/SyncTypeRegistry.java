package dev.overgrown.sync.registry.factory;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.set_no_gravity.SetNoGravityAction;
import dev.overgrown.sync.factory.compatibility.aspectslib.SetEntityAspectsPower;
import dev.overgrown.sync.factory.compatibility.aspectslib.condition.HasAspectCondition;
import dev.overgrown.sync.factory.action.bientity.add_to_entity_set.AddToEntitySetAction;
import dev.overgrown.sync.factory.action.bientity.remove_from_entity_set.RemoveFromEntitySetAction;
import dev.overgrown.sync.factory.action.block.ghost_block.GhostBlockAction;
import dev.overgrown.sync.factory.action.block.spawn_entity_block.SpawnEntityBlockAction;
import dev.overgrown.sync.factory.action.entity.action_on_entity_set.ActionOnEntitySetAction;
import dev.overgrown.sync.factory.action.entity.custom_projectile.CustomProjectileAction;
import dev.overgrown.sync.factory.action.entity.grant_all_powers.GrantAllPowersAction;
import dev.overgrown.sync.factory.action.entity.summons.SetSummonMaxLifeAction;
import dev.overgrown.sync.factory.action.entity.summons.SummonCloneAction;
import dev.overgrown.sync.factory.action.entity.summons.SummonMinionAction;
import dev.overgrown.sync.factory.action.entity.print.PrintAction;
import dev.overgrown.sync.factory.action.entity.radial_menu.RadialMenuAction;
import dev.overgrown.sync.factory.action.entity.remove_power.RemovePowerAction;
import dev.overgrown.sync.factory.action.entity.revoke_all_powers.RevokeAllPowersAction;
import dev.overgrown.sync.factory.action.entity.teleportation.RandomTeleportAction;
import dev.overgrown.sync.factory.action.entity.teleportation.SaveLocationAction;
import dev.overgrown.sync.factory.action.entity.teleportation.TeleportToLocationAction;
import dev.overgrown.sync.factory.condition.bientity.in_entity_set.InEntitySetCondition;
import dev.overgrown.sync.factory.condition.entity.entity_in_radius.EntityInRadiusCondition;
import dev.overgrown.sync.factory.condition.entity.entity_set_size.EntitySetSizeCondition;
import dev.overgrown.sync.factory.condition.entity.has_command_tag.HasCommandTagCondition;
import dev.overgrown.sync.factory.condition.entity.in_pose.InPoseCondition;
import dev.overgrown.sync.factory.condition.entity.key_pressed.KeyPressedCondition;
import dev.overgrown.sync.factory.condition.entity.player_model_type.PlayerModelTypeCondition;
import dev.overgrown.sync.factory.condition.item.fuel.FuelCondition;
import dev.overgrown.sync.factory.power.type.action_on_death.ActionOnDeathPower;
import dev.overgrown.sync.factory.power.type.custom_projectile.CustomProjectilePower;
import dev.overgrown.sync.factory.power.type.emissive.EmissivePower;
import dev.overgrown.sync.factory.power.type.entity_set.EntitySetPower;
import dev.overgrown.sync.factory.power.type.entity_texture_overlay.EntityTextureOverlayPower;
import dev.overgrown.sync.factory.power.type.edible_item.EdibleItemPower;
import dev.overgrown.sync.factory.power.type.flip_model.FlipModelPower;
import dev.overgrown.sync.factory.power.type.mobs_ignore.MobsIgnorePower;
import dev.overgrown.sync.factory.power.type.modify_enchantment_level.ModifyEnchantmentLevelPower;
import dev.overgrown.sync.factory.power.type.modify_model_parts.ModifyModelPartsPower;
import dev.overgrown.sync.factory.power.type.modify_player_model.ModifyPlayerModelPower;
import dev.overgrown.sync.factory.power.type.pose.PosePower;
import dev.overgrown.sync.factory.power.type.prevent_sprinting_particles.PreventSprintingParticlesPower;
import dev.overgrown.sync.factory.power.type.sprinting.SprintingPower;
import dev.overgrown.sync.registry.factory.utils.ApoliRegistryHelper;

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
        ApoliRegistryHelper.registerPowerFactory(ModifyPlayerModelPower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(PosePower.getFactory());
        ApoliRegistryHelper.registerPowerFactory(PreventSprintingParticlesPower.getFactory());
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
        ApoliRegistryHelper.registerEntityAction(SetNoGravityAction.getFactory());
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