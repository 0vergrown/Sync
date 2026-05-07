package dev.overgrown.sync.power.type.action_on_key_sequence;

import dev.overgrown.sync.condition.type.entity.key_pressed.util.PlayerKeyState;
import dev.overgrown.sync.condition.type.entity.key_pressed.util.PlayerKeyStateManager;
import dev.overgrown.sync.power.type.action_on_key_sequence.data.FunctionalKey;
import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.HudRendered;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ActionOnKeySequencePowerType extends PowerType implements HudRendered {

    public static final SerializableDataType<FunctionalKey> FUNCTIONAL_KEY = SerializableDataType.compound(
        new SerializableData()
            .add("key", SerializableDataTypes.STRING)
            .add("continuous", SerializableDataTypes.BOOLEAN, false)
            .add("action", EntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new FunctionalKey(
            data.get("key"),
            data.get("continuous"),
            data.get("action")
        ),
        (fk, sd) -> sd.instance()
            .set("key", fk.key())
            .set("continuous", fk.continuous())
            .set("action", fk.action())
    );

    public static final SerializableDataType<List<FunctionalKey>> FUNCTIONAL_KEYS = FUNCTIONAL_KEY.list();

    public static final TypedDataObjectFactory<ActionOnKeySequencePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("success_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("fail_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("cooldown", SerializableDataTypes.INT, 0)
            .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
            .add("keys", FUNCTIONAL_KEYS)
            .add("key_sequence", SerializableDataTypes.STRINGS),
        (data, condition) -> new ActionOnKeySequencePowerType(
            data.get("success_action"),
            data.get("fail_action"),
            data.get("cooldown"),
            data.get("hud_render"),
            data.get("keys"),
            data.get("key_sequence"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("success_action", powerType.successAction)
            .set("fail_action", powerType.failAction)
            .set("cooldown", powerType.cooldownDuration)
            .set("hud_render", powerType.hudRender)
            .set("keys", powerType.keys)
            .set("key_sequence", powerType.keySequence)
    );

    private final Optional<EntityAction> successAction;
    private final Optional<EntityAction> failAction;
    private final int cooldownDuration;
    private final HudRender hudRender;
    private final List<FunctionalKey> keys;
    private final List<String> keySequence;

    private int currentCooldown = 0;
    private int sequenceProgress = 0;
    private Map<String, Boolean> prevKeyStates = null;

    public ActionOnKeySequencePowerType(Optional<EntityAction> successAction,
                                        Optional<EntityAction> failAction,
                                        int cooldownDuration,
                                        HudRender hudRender,
                                        List<FunctionalKey> keys,
                                        List<String> keySequence,
                                        Optional<EntityCondition> condition) {
        super(condition);
        this.successAction = successAction;
        this.failAction = failAction;
        this.cooldownDuration = cooldownDuration;
        this.hudRender = hudRender;
        this.keys = keys;
        this.keySequence = keySequence;
        setTicking();
    }

    private static boolean isPressed(ServerPlayerEntity player, String keyId) {
        PlayerKeyState state = PlayerKeyStateManager.get(player);
        return state != null && state.isPressed(keyId);
    }

    @Override
    public void serverTick() {
        Entity entity = getHolder();
        if (entity.getWorld().isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        if (prevKeyStates == null) {
            prevKeyStates = new HashMap<>();
            for (FunctionalKey fk : keys) {
                prevKeyStates.put(fk.key(), isPressed(player, fk.key()));
            }
            return;
        }

        if (currentCooldown > 0) {
            currentCooldown--;
            for (FunctionalKey fk : keys) {
                prevKeyStates.put(fk.key(), isPressed(player, fk.key()));
            }
            return;
        }

        List<String> firedKeyNames = new ArrayList<>();

        for (FunctionalKey fk : keys) {
            boolean isNowPressed = isPressed(player, fk.key());
            boolean wasPrevPressed = prevKeyStates.getOrDefault(fk.key(), false);
            prevKeyStates.put(fk.key(), isNowPressed);

            boolean fires = fk.continuous() ? isNowPressed : (isNowPressed && !wasPrevPressed);
            if (!fires) continue;

            fk.action().ifPresent(act -> act.accept(new EntityActionContext(entity)));
            firedKeyNames.add(fk.key());
        }

        if (keySequence.isEmpty() || firedKeyNames.isEmpty()) return;

        String expectedKey = keySequence.get(sequenceProgress);
        boolean correctKeyFired = firedKeyNames.contains(expectedKey);

        if (correctKeyFired) {
            sequenceProgress++;
            if (sequenceProgress >= keySequence.size()) {
                successAction.ifPresent(act -> act.accept(new EntityActionContext(entity)));
                currentCooldown = cooldownDuration;
                sequenceProgress = 0;
            }
        } else if (sequenceProgress > 0) {
            failAction.ifPresent(act -> act.accept(new EntityActionContext(entity)));
            String firstKey = keySequence.get(0);
            sequenceProgress = firedKeyNames.contains(firstKey) ? 1 : 0;
        }
    }

    @Override
    public HudRender getRenderSettings() {
        return hudRender;
    }

    @Override
    public float getFill() {
        if (cooldownDuration <= 0) return 1.0f;
        return 1.0f - (float) currentCooldown / (float) cooldownDuration;
    }

    @Override
    public boolean shouldRender() {
        return hudRender.shouldRender();
    }

    @Override
    public NbtElement toTag() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("cooldown", currentCooldown);
        nbt.putInt("sequenceProgress", sequenceProgress);
        return nbt;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if (tag instanceof NbtCompound nbt) {
            currentCooldown = nbt.getInt("cooldown");
            sequenceProgress = nbt.getInt("sequenceProgress");
        }
        prevKeyStates = null;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.ACTION_ON_KEY_SEQUENCE;
    }
}
