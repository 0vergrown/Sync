package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.condition.type.entity.attached_to_rope.AttachedToRopeEntityConditionType;
import dev.overgrown.sync.condition.type.entity.disguised.DisguisedEntityConditionType;
import dev.overgrown.sync.condition.type.entity.in_pose.InPoseEntityConditionType;
import dev.overgrown.sync.condition.type.entity.is_selected_stolen_power.IsSelectedStolenPowerEntityConditionType;
import dev.overgrown.sync.condition.type.entity.key_pressed.KeyPressedEntityConditionType;
import dev.overgrown.sync.condition.type.entity.mod_loaded.ModLoadedEntityConditionType;
import dev.overgrown.sync.condition.type.entity.perspective.PerspectiveEntityConditionType;
import dev.overgrown.sync.condition.type.entity.player_model_type.PlayerModelTypeEntityConditionType;
import dev.overgrown.sync.condition.type.entity.velocity.VelocityEntityConditionType;
import dev.overgrown.sync.registry.utils.ApoliRegistryHelper;
import io.github.apace100.apoli.condition.ConditionConfiguration;

public class SyncEntityConditionTypes {

    public static final ConditionConfiguration<AttachedToRopeEntityConditionType> ATTACHED_TO_ROPE = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("attached_to_rope"), AttachedToRopeEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<DisguisedEntityConditionType> DISGUISED = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("disguised"), DisguisedEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<InPoseEntityConditionType> IN_POSE = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("in_pose"), InPoseEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<IsSelectedStolenPowerEntityConditionType> IS_SELECTED_STOLEN_POWER = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("is_selected_stolen_power"), IsSelectedStolenPowerEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<KeyPressedEntityConditionType> KEY_PRESSED = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("key_pressed"), KeyPressedEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<ModLoadedEntityConditionType> MOD_LOADED = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("mod_loaded"), ModLoadedEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<PerspectiveEntityConditionType> PERSPECTIVE = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("perspective"), PerspectiveEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<PlayerModelTypeEntityConditionType> PLAYER_MODEL_TYPE = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("player_model_type"), PlayerModelTypeEntityConditionType.DATA_FACTORY)
    );

    public static final ConditionConfiguration<VelocityEntityConditionType> VELOCITY = ApoliRegistryHelper.registerEntityCondition(
        ConditionConfiguration.of(Sync.identifier("velocity"), VelocityEntityConditionType.DATA_FACTORY)
    );

    public static void register() {
    }
}
