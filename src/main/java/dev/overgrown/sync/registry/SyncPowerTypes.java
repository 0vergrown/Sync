package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.power.type.action_on_key_sequence.ActionOnKeySequencePowerType;
import dev.overgrown.sync.power.type.action_on_sending_message.ActionOnSendingMessagePowerType;
import dev.overgrown.sync.power.type.body_part_damage_modifier.BodyPartDamageModifierPowerType;
import dev.overgrown.sync.power.type.custom_projectile.CustomProjectilePowerType;
import dev.overgrown.sync.power.type.emissive.EmissivePowerType;
import dev.overgrown.sync.power.type.energy_swirl.EnergySwirlPowerType;
import dev.overgrown.sync.power.type.entity_texture_overlay.EntityTextureOverlayPowerType;
import dev.overgrown.sync.power.type.flip_model.FlipModelPowerType;
import dev.overgrown.sync.power.type.mobs_ignore.MobsIgnorePowerType;
import dev.overgrown.sync.power.type.modify_enchantment_damage_dealt.ModifyEnchantmentDamageDealtPowerType;
import dev.overgrown.sync.power.type.modify_enchantment_damage_taken.ModifyEnchantmentDamageTakenPowerType;
import dev.overgrown.sync.power.type.modify_label_render.ModifyLabelRenderPowerType;
import dev.overgrown.sync.power.type.modify_model_parts.ModifyModelPartsPowerType;
import dev.overgrown.sync.power.type.modify_player_model.ModifyPlayerModelPowerType;
import dev.overgrown.sync.power.type.prevent_creative_flight.PreventCreativeFlightPowerType;
import dev.overgrown.sync.power.type.prevent_sprinting_particles.PreventSprintingParticlesPowerType;
import dev.overgrown.sync.power.type.prevent_teleport.PreventTeleportPowerType;
import dev.overgrown.sync.power.type.sprinting.SprintingPowerType;
import dev.overgrown.sync.registry.utils.ApoliRegistryHelper;
import io.github.apace100.apoli.power.PowerConfiguration;

public class SyncPowerTypes {

    public static final PowerConfiguration<ActionOnKeySequencePowerType> ACTION_ON_KEY_SEQUENCE = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("action_on_key_sequence"), ActionOnKeySequencePowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<ActionOnSendingMessagePowerType> ACTION_ON_SENDING_MESSAGE = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("action_on_sending_message"), ActionOnSendingMessagePowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<BodyPartDamageModifierPowerType> BODY_PART_DAMAGE_MODIFIER = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("body_part_damage_modifier"), BodyPartDamageModifierPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<CustomProjectilePowerType> CUSTOM_PROJECTILE = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("custom_projectile"), CustomProjectilePowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<EmissivePowerType> EMISSIVE = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("emissive"), EmissivePowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<EnergySwirlPowerType> ENERGY_SWIRL = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("energy_swirl"), EnergySwirlPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<EntityTextureOverlayPowerType> ENTITY_TEXTURE_OVERLAY = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("entity_texture_overlay"), EntityTextureOverlayPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<FlipModelPowerType> FLIP_MODEL = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("flip_model"), FlipModelPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<MobsIgnorePowerType> MOBS_IGNORE = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("mobs_ignore"), MobsIgnorePowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<ModifyEnchantmentDamageDealtPowerType> MODIFY_ENCHANTMENT_DAMAGE_DEALT = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("modify_enchantment_damage_dealt"), ModifyEnchantmentDamageDealtPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<ModifyEnchantmentDamageTakenPowerType> MODIFY_ENCHANTMENT_DAMAGE_TAKEN = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("modify_enchantment_damage_taken"), ModifyEnchantmentDamageTakenPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<ModifyLabelRenderPowerType> MODIFY_LABEL_RENDER = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("modify_label_render"), ModifyLabelRenderPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<ModifyModelPartsPowerType> MODIFY_MODEL_PARTS = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("modify_model_parts"), ModifyModelPartsPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<ModifyPlayerModelPowerType> MODIFY_PLAYER_MODEL = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("modify_player_model"), ModifyPlayerModelPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<PreventCreativeFlightPowerType> PREVENT_CREATIVE_FLIGHT = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("prevent_creative_flight"), PreventCreativeFlightPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<PreventSprintingParticlesPowerType> PREVENT_SPRINTING_PARTICLES = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("prevent_sprinting_particles"), PreventSprintingParticlesPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<PreventTeleportPowerType> PREVENT_TELEPORT = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("prevent_teleport"), PreventTeleportPowerType.DATA_FACTORY)
    );

    public static final PowerConfiguration<SprintingPowerType> SPRINTING = ApoliRegistryHelper.registerPower(
        PowerConfiguration.of(Sync.identifier("sprinting"), SprintingPowerType.DATA_FACTORY)
    );

    public static void register() {
    }
}
