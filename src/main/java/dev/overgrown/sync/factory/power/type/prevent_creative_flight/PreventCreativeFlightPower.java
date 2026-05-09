package dev.overgrown.sync.factory.power.type.prevent_creative_flight;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

/**
 * Prevents a player from using creative (ability) flight.
 *
 * <p>Does <strong>not</strong> interfere with players who are genuinely in Creative or
 * Spectator game-mode, since those modes force {@code allowFlying} on regardless.  It
 * only strips the ability that was granted by a power such as Apoli's built-in
 * {@code apoli:creative_flight} or PAL's {@code VanillaAbilities.ALLOW_FLYING}.</p>
 *
 * <p>The optional {@code entity_action} is executed once, at the moment the player is
 * midair and the power cancels their flight — useful for triggering a fall-damage
 * warning, a sound, or a particle burst.</p>
 *
 * <p>JSON example:</p>
 * <pre>{@code
 * {
 *   "type": "sync:prevent_creative_flight",
 *   "entity_action": {
 *     "type": "apoli:execute_command",
 *     "command": "playsound minecraft:entity.bat.hurt master @s"
 *   }
 * }
 * }</pre>
 */
public class PreventCreativeFlightPower extends Power {

    private final Consumer<Entity> entityAction;

    public PreventCreativeFlightPower(PowerType<?> type, LivingEntity entity, Consumer<Entity> entityAction) {
        super(type, entity);
        this.entityAction = entityAction;
        // We need server-side ticking to poll and override ability flags every tick.
        setTicking(true);
    }

    @Override
    public void tick() {
        // Powers tick on both sides; abilities only exist server-side on ServerPlayerEntity.
        if (!(entity instanceof ServerPlayerEntity player)) return;

        // Let the game-mode itself keep flying alive for Creative / Spectator players.
        // Those modes re-enable allowFlying each tick anyway, so fighting them is pointless.
        if (player.isCreative() || player.isSpectator()) return;

        boolean wasFlying     = player.getAbilities().flying;
        boolean hadAllowFlying = player.getAbilities().allowFlying;

        if (wasFlying || hadAllowFlying) {
            // Fire the entity_action only at the instant the player was actively flying
            // (i.e. airborne and using the flight ability), not just while the flag is set.
            if (wasFlying && entityAction != null) {
                entityAction.accept(player);
            }

            player.getAbilities().flying     = false;
            player.getAbilities().allowFlying = false;
            // Sync ability flags to the client so it stops processing flight input.
            player.sendAbilitiesUpdate();
        }
    }

    public static PowerFactory<?> getFactory() {
        return new PowerFactory<>(
                Sync.identifier("prevent_creative_flight"),
                new SerializableData()
                        .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
                data -> (type, player) -> new PreventCreativeFlightPower(
                        type,
                        player,
                        data.get("entity_action")
                )
        ).allowCondition();
    }
}