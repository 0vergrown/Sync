package dev.overgrown.sync.factory.power.type.action_on_key_sequence;

import com.google.gson.JsonSyntaxException;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import dev.overgrown.sync.factory.power.type.action_on_key_sequence.data.FunctionalKey;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.HudRendered;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ActionOnKeySequencePower extends Power implements HudRendered {
    public static final SerializableDataType<FunctionalKey> FUNCTIONAL_KEY =
            new SerializableDataType<>(
                    FunctionalKey.class,
                    (buf, fk) -> {
                        Active.Key ak = new Active.Key();
                        ak.key = fk.key();
                        ak.continuous = fk.continuous();
                        ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY.send(buf, ak);

                        boolean hasAction = fk.action() != null;
                        buf.writeBoolean(hasAction);
                        if (hasAction) {
                            ApoliDataTypes.ENTITY_ACTION.send(buf, fk.action());
                        }
                    },

                    (buf) -> {
                        Active.Key ak = ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY.receive(buf);
                        boolean hasAction = buf.readBoolean();
                        Consumer<Entity> action = hasAction
                                ? ApoliDataTypes.ENTITY_ACTION.receive(buf)
                                : null;
                        return new FunctionalKey(ak.key, ak.continuous, action);
                    },

                    (json) -> {
                        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                            return new FunctionalKey(json.getAsString(), false, null);
                        }
                        if (json.isJsonObject()) {
                            SerializableData sd = new SerializableData()
                                    .add("key", SerializableDataTypes.STRING)
                                    .add("continuous", SerializableDataTypes.BOOLEAN, false)
                                    .add("action", ApoliDataTypes.ENTITY_ACTION,  null);
                            SerializableData.Instance inst = sd.read(json.getAsJsonObject());
                            return new FunctionalKey(
                                    inst.getString("key"),
                                    inst.getBoolean("continuous"),
                                    inst.get("action")
                            );
                        }
                        throw new JsonSyntaxException(
                                "A FunctionalKey must be a string (key name) or an object " +
                                        "with 'key', optional 'continuous', and optional 'action'.");
                    }
            );

    private final Consumer<Entity> successAction;
    private final Consumer<Entity> failAction;
    private final int cooldownDuration;
    private final HudRender hudRender;
    private final List<FunctionalKey> keys;
    private final List<String> keySequence;
    private int currentCooldown = 0;
    private int sequenceProgress = 0;

    private Map<String, Boolean> prevKeyStates = null;
    public ActionOnKeySequencePower(PowerType<?> type,
                                    LivingEntity entity,
                                    Consumer<Entity> successAction,
                                    Consumer<Entity> failAction,
                                    int cooldownDuration,
                                    HudRender hudRender,
                                    List<FunctionalKey> keys,
                                    List<String> keySequence) {
        super(type, entity);
        this.successAction = successAction;
        this.failAction = failAction;
        this.cooldownDuration = cooldownDuration;
        this.hudRender = hudRender;
        this.keys = keys;
        this.keySequence = keySequence;

        this.setTicking();
    }

    @Override
    public void tick() {
        if (entity.getWorld().isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (prevKeyStates == null) {
            prevKeyStates = new HashMap<>();
            for (FunctionalKey fk : keys) {
                prevKeyStates.put(fk.key(),
                        KeyPressManager.getKeyState(player.getUuid(), fk.key(), true));
            }
            return;
        }

        if (currentCooldown > 0) {
            currentCooldown--;
            for (FunctionalKey fk : keys) {
                prevKeyStates.put(fk.key(),
                        KeyPressManager.getKeyState(player.getUuid(), fk.key(), true));
            }
            return;
        }

        List<String> firedKeyNames = new ArrayList<>();

        for (FunctionalKey fk : keys) {
            boolean isNowPressed   = KeyPressManager.getKeyState(player.getUuid(), fk.key(), true);
            boolean wasPrevPressed = prevKeyStates.getOrDefault(fk.key(), false);
            prevKeyStates.put(fk.key(), isNowPressed);

            boolean fires = fk.continuous()
                    ? isNowPressed
                    : (isNowPressed && !wasPrevPressed);

            if (!fires) continue;

            if (fk.action() != null) {
                fk.action().accept(entity);
            }
            firedKeyNames.add(fk.key());
        }

        if (keySequence.isEmpty() || firedKeyNames.isEmpty()) return;

        String  expectedKey = keySequence.get(sequenceProgress);
        boolean correctKeyFired = firedKeyNames.contains(expectedKey);

        if (correctKeyFired) {
            sequenceProgress++;
            if (sequenceProgress >= keySequence.size()) {
                if (successAction != null) {
                    successAction.accept(entity);
                }
                currentCooldown  = cooldownDuration;
                sequenceProgress = 0;
            }

        } else if (sequenceProgress > 0) {
            if (failAction != null) {
                failAction.accept(entity);
            }
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
        nbt.putInt("cooldown",         currentCooldown);
        nbt.putInt("sequenceProgress", sequenceProgress);
        return nbt;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if (tag instanceof NbtCompound nbt) {
            currentCooldown  = nbt.getInt("cooldown");
            sequenceProgress = nbt.getInt("sequenceProgress");
        }
        prevKeyStates = null;
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("action_on_key_sequence"),
                new SerializableData()
                        .add("success_action", ApoliDataTypes.ENTITY_ACTION,null)
                        .add("fail_action", ApoliDataTypes.ENTITY_ACTION,null)
                        .add("cooldown", SerializableDataTypes.INT,0)
                        .add("hud_render",ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                        .add("keys", SerializableDataType.list(FUNCTIONAL_KEY))
                        .add("key_sequence", SerializableDataTypes.STRINGS),
                data -> (type, entity) -> new ActionOnKeySequencePower(
                        type,
                        entity,
                        data.get("success_action"),
                        data.get("fail_action"),
                        data.getInt("cooldown"),
                        data.get("hud_render"),
                        data.get("keys"),
                        data.get("key_sequence")
                )
        ).allowCondition();
    }
}