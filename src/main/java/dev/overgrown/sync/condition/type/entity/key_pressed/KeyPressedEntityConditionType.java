package dev.overgrown.sync.condition.type.entity.key_pressed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import dev.overgrown.sync.condition.type.entity.key_pressed.util.PlayerKeyState;
import dev.overgrown.sync.condition.type.entity.key_pressed.util.PlayerKeyStateManager;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.keybinding.KeyBindingReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class KeyPressedEntityConditionType extends EntityConditionType {

    public static final SerializableDataType<KeyBindingReference> KEY = SerializableDataType.compound(
        new SerializableData()
            .add("key", SerializableDataTypes.STRING, null)
            .addFunctionedDefault("id", SerializableDataTypes.STRING, data -> data.get("key"))
            .add("continuous", SerializableDataTypes.BOOLEAN, true)
            .validate(MiscUtil.validateAllFieldsPresent("id")),
        data -> new KeyBindingReference(data.get("id"), data.get("continuous")),
        (keyBindingReference, serializableData) -> serializableData.instance()
            .set("id", keyBindingReference.id())
            .set("continuous", keyBindingReference.continuous())
    );

    public static final SerializableDataType<KeyBindingReference> BACKWARDS_COMPATIBLE_KEY = SerializableDataType.of(
        new Codec<>() {
            @Override
            public <T> DataResult<com.mojang.datafixers.util.Pair<KeyBindingReference, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<String> stringInput = ops.getStringValue(input);
                if (stringInput.isSuccess()) {
                    return stringInput
                        .map(id -> new KeyBindingReference(id, true))
                        .map(key -> com.mojang.datafixers.util.Pair.of(key, input));
                } else {
                    return KEY.codec().decode(ops, input);
                }
            }

            @Override
            public <T> DataResult<T> encode(KeyBindingReference input, DynamicOps<T> ops, T prefix) {
                return KEY.codec().encode(input, ops, prefix);
            }
        },
        KEY.packetCodec()
    );

    public static final TypedDataObjectFactory<KeyPressedEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("key", BACKWARDS_COMPATIBLE_KEY)
            .add("continuous", SerializableDataTypes.BOOLEAN, true),
        data -> new KeyPressedEntityConditionType(
            data.get("key"),
            data.get("continuous")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("key", conditionType.key)
            .set("continuous", conditionType.continuous)
    );

    private final KeyBindingReference key;
    private final boolean continuous;

    public KeyPressedEntityConditionType(KeyBindingReference key, boolean continuous) {
        this.key = key;
        this.continuous = continuous;
    }

    @Override
    public boolean test(EntityConditionContext context) {

        if (!(context.entity() instanceof PlayerEntity player)) {
            return false;
        }

        PlayerKeyState state = PlayerKeyStateManager.get(player);
        if (state == null) {
            return false;
        }

        boolean effectiveContinuous = continuous && key.continuous();
        String keyId = key.id();
        return effectiveContinuous ? state.isPressed(keyId) : state.wasJustPressed(keyId);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.KEY_PRESSED;
    }
}
