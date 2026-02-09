package dev.overgrown.sync.factory.power.type.sprinting;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.condition.entity.key_pressed.utils.KeyPressManager;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class SprintingPower extends Power {

    private final boolean requiresInput;

    public SprintingPower(PowerType<?> type, LivingEntity entity, boolean requiresInput) {
        super(type, entity);
        this.requiresInput = requiresInput;
    }

    public boolean shouldSprint() {
        if (!this.isActive()) {
            return false;
        }

        // Use the MovingEntity interface to check if entity is actually moving
        boolean isMoving = ((MovingEntity) entity).isMoving();

        // If not moving, don't sprint (prevents the toggle bug)
        if (!isMoving) {
            return false;
        }

        // If input is required, check for sprint keys
        if (requiresInput) {
            if (entity instanceof PlayerEntity player) {
                // Check for key.sprint keybinding
                boolean sprintKeyPressed = KeyPressManager.getKeyState(player.getUuid(), "key.sprint", true);

                // Check for key.forward keybinding (for double-tap sprint)
                boolean forwardKeyPressed = KeyPressManager.getKeyState(player.getUuid(), "key.forward", true);

                return sprintKeyPressed || forwardKeyPressed;
            }
            return false;
        }

        // If no input required and entity is moving, always sprint
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        // Force sprinting state every tick if conditions are met
        if (shouldSprint()) {
            entity.setSprinting(true);
        }
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("sprinting"),
                new SerializableData()
                        .add("requires_input", SerializableDataTypes.BOOLEAN, false),
                data -> (powerType, entity) -> new SprintingPower(
                        powerType,
                        entity,
                        data.getBoolean("requires_input")
                )
        ).allowCondition();
    }
}