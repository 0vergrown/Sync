package dev.overgrown.sync.factory.power.type.prevent_teleport;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

/**
 * Prevents the holder from being teleported by any means:
 * /tp, /spawn, spectator menus, portals, Apoli power types, entity actions, etc.
 *
 * <p>The optional {@code entity_action} fires once per blocked teleport attempt.</p>
 *
 * <p>JSON example:</p>
 * <pre>{@code
 * {
 *   "type": "sync:prevent_teleport",
 *   "entity_action": {
 *     "type": "apoli:execute_command",
 *     "command": "playsound minecraft:block.note_block.bass master @s"
 *   }
 * }
 * }</pre>
 */
public class PreventTeleportPower extends Power {

    private final Consumer<Entity> entityAction;

    public PreventTeleportPower(PowerType<?> type, LivingEntity entity, Consumer<Entity> entityAction) {
        super(type, entity);
        this.entityAction = entityAction;
    }

    /** Called by the mixin each time a teleport is blocked. */
    public void onTeleportPrevented() {
        if (entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("prevent_teleport"),
                new SerializableData()
                        .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
                data -> (type, player) -> new PreventTeleportPower(
                        type,
                        player,
                        data.get("entity_action")
                )
        ).allowCondition();
    }
}