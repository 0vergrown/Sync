package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.action.type.entity.attach_rope.AttachRopeEntityActionType;
import dev.overgrown.sync.action.type.entity.attach_rope_entity.AttachRopeEntityEntityActionType;
import dev.overgrown.sync.action.type.entity.change_selected_slot.ChangeSelectedSlotEntityActionType;
import dev.overgrown.sync.action.type.entity.change_slot.ChangeSlotEntityActionType;
import dev.overgrown.sync.action.type.entity.meta.LoopEntityActionType;
import dev.overgrown.sync.action.type.meta.loop.LoopMetaActionType;
import io.github.apace100.apoli.action.EntityAction;
import dev.overgrown.sync.action.type.entity.custom_projectile.CustomProjectileEntityActionType;
import dev.overgrown.sync.action.type.entity.cycle_stolen_power.CycleStolenPowerEntityActionType;
import dev.overgrown.sync.action.type.entity.disguise_as.DisguiseAsEntityActionType;
import dev.overgrown.sync.action.type.entity.disguise_as_player.DisguiseAsPlayerEntityActionType;
import dev.overgrown.sync.action.type.entity.grant_all_powers.GrantAllPowersEntityActionType;
import dev.overgrown.sync.action.type.entity.item_cooldown.ItemCooldownEntityActionType;
import dev.overgrown.sync.action.type.entity.print.PrintEntityActionType;
import dev.overgrown.sync.action.type.entity.radial_menu.RadialMenuEntityActionType;
import dev.overgrown.sync.action.type.entity.remove_disguise.RemoveDisguiseEntityActionType;
import dev.overgrown.sync.action.type.entity.save_location.SaveLocationEntityActionType;
import dev.overgrown.sync.action.type.entity.set_no_gravity.SetNoGravityEntityActionType;
import dev.overgrown.sync.action.type.entity.summons.SetSummonMaxLifeEntityActionType;
import dev.overgrown.sync.action.type.entity.summons.SummonCloneEntityActionType;
import dev.overgrown.sync.action.type.entity.summons.SummonMinionEntityActionType;
import dev.overgrown.sync.action.type.entity.teleport_to_location.TeleportToLocationEntityActionType;
import dev.overgrown.sync.action.type.entity.teleport_to_spawn.TeleportToSpawnEntityActionType;
import dev.overgrown.sync.action.type.entity.toggle_transfer_mode.ToggleTransferModeEntityActionType;
import dev.overgrown.sync.action.type.entity.use_selected_stolen_power.UseSelectedStolenPowerEntityActionType;
import dev.overgrown.sync.registry.utils.ApoliRegistryHelper;
import io.github.apace100.apoli.action.ActionConfiguration;

public class SyncEntityActionTypes {

    public static final ActionConfiguration<AttachRopeEntityActionType> ATTACH_ROPE = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("attach_rope"), AttachRopeEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<AttachRopeEntityEntityActionType> ATTACH_ROPE_ENTITY = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("attach_rope_entity"), AttachRopeEntityEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<ChangeSelectedSlotEntityActionType> CHANGE_SELECTED_SLOT = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("change_selected_slot"), ChangeSelectedSlotEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<ChangeSlotEntityActionType> CHANGE_SLOT = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("change_slot"), ChangeSlotEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<LoopEntityActionType> LOOP = ApoliRegistryHelper.registerEntityAction(
        LoopMetaActionType.createConfiguration(EntityAction.DATA_TYPE, LoopEntityActionType::new)
    );

    public static final ActionConfiguration<CustomProjectileEntityActionType> CUSTOM_PROJECTILE = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("custom_projectile"), CustomProjectileEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<CycleStolenPowerEntityActionType> CYCLE_STOLEN_POWER = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("cycle_stolen_power"), CycleStolenPowerEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<DisguiseAsEntityActionType> DISGUISE_AS = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("disguise_as"), DisguiseAsEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<DisguiseAsPlayerEntityActionType> DISGUISE_AS_PLAYER = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("disguise_as_player"), DisguiseAsPlayerEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<GrantAllPowersEntityActionType> GRANT_ALL_POWERS = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("grant_all_powers"), GrantAllPowersEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<ItemCooldownEntityActionType> ITEM_COOLDOWN = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("item_cooldown"), ItemCooldownEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<PrintEntityActionType> PRINT = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("print"), PrintEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<RadialMenuEntityActionType> RADIAL_MENU = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("radial_menu"), RadialMenuEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<RemoveDisguiseEntityActionType> REMOVE_DISGUISE = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("remove_disguise"), RemoveDisguiseEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<SaveLocationEntityActionType> SAVE_LOCATION = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("save_location"), SaveLocationEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<SetNoGravityEntityActionType> SET_NO_GRAVITY = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("set_no_gravity"), SetNoGravityEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<SetSummonMaxLifeEntityActionType> SET_SUMMON_MAX_LIFE_TICKS = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("set_summon_max_life_ticks"), SetSummonMaxLifeEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<SummonMinionEntityActionType> SUMMON_MINION = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("summon_minion"), SummonMinionEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<SummonCloneEntityActionType> SUMMON_CLONE = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("summon_clone"), SummonCloneEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<TeleportToLocationEntityActionType> TELEPORT_TO_LOCATION = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("teleport_to_location"), TeleportToLocationEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<TeleportToSpawnEntityActionType> TELEPORT_TO_SPAWN = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("teleport_to_spawn"), TeleportToSpawnEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<ToggleTransferModeEntityActionType> TOGGLE_TRANSFER_MODE = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("toggle_transfer_mode"), ToggleTransferModeEntityActionType.DATA_FACTORY)
    );

    public static final ActionConfiguration<UseSelectedStolenPowerEntityActionType> USE_SELECTED_STOLEN_POWER = ApoliRegistryHelper.registerEntityAction(
        ActionConfiguration.of(Sync.identifier("use_selected_stolen_power"), UseSelectedStolenPowerEntityActionType.DATA_FACTORY)
    );

    public static void register() {
    }
}
