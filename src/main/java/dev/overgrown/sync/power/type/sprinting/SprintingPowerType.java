package dev.overgrown.sync.power.type.sprinting;

import dev.overgrown.sync.condition.type.entity.key_pressed.util.PlayerKeyState;
import dev.overgrown.sync.condition.type.entity.key_pressed.util.PlayerKeyStateManager;
import dev.overgrown.sync.registry.SyncPowerTypes;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SprintingPowerType extends PowerType {

    public static final TypedDataObjectFactory<SprintingPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("requires_input", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new SprintingPowerType(
            data.get("requires_input"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("requires_input", powerType.requiresInput)
    );

    private final boolean requiresInput;

    public SprintingPowerType(boolean requiresInput, Optional<EntityCondition> condition) {
        super(condition);
        this.requiresInput = requiresInput;
        setTicking();
    }

    public boolean shouldSprint() {
        if (!isActive()) return false;

        if (!((MovingEntity) getHolder()).apoli$isMoving()) return false;

        if (requiresInput) {
            if (!(getHolder() instanceof PlayerEntity player)) return false;
            PlayerKeyState state = PlayerKeyStateManager.get(player);
            if (state == null) return false;
            return state.isPressed("key.sprint") || state.isPressed("key.forward");
        }

        return true;
    }

    @Override
    public void serverTick() {
        if (shouldSprint()) {
            getHolder().setSprinting(true);
        }
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return SyncPowerTypes.SPRINTING;
    }
}
