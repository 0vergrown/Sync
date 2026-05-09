package dev.overgrown.sync.factory.action.meta.loop;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.Scheduler;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

/**
 * A generic meta action that executes an {@code action} for a given number of
 * iterations, spacing each iteration out by {@code ticks} ticks so they never
 * all fire on the same game tick.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code value}: Number of loop iterations (default: 1)</li>
 *   <li>{@code ticks}: Tick interval between iterations (minimum: 1, default: 1)</li>
 *   <li>{@code before_action}: (optional) fires once, immediately, before the first iteration</li>
 *   <li>{@code action}: (optional) fires once per iteration</li>
 *   <li>{@code after_action}: (optional) fires once after the final iteration completes</li>
 * </ul>
 *
 * <p>Timing example with {@code value = 3} and {@code ticks = 5}:
 * <pre>
 *   tick 0: before_action
 *   tick 5: action (iteration 1)
 *   tick 10: action (iteration 2)
 *   tick 15: action (iteration 3) &amp; after_action
 * </pre>
 *
 * <p>When {@code value} is 0 the loop body never runs; {@code before_action}
 * still fires immediately and {@code after_action} fires at tick 0 (end of the
 * current tick), which is consistent with a zero-iteration loop completing
 * instantly.
 */
public class LoopAction {

    /**
     * One shared scheduler for all LoopAction instances.
     * The Scheduler hooks into {@link net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents#END_SERVER_TICK}
     * internally, so a single instance is safe to reuse.
     */
    private static final Scheduler SCHEDULER = new Scheduler();

    // -------------------------------------------------------------------------
    // Core logic
    // -------------------------------------------------------------------------
    public static <T> void action(SerializableData.Instance data, T t) {
        final int value = Math.max(0, data.getInt("value"));
        final int ticks = Math.max(1, data.getInt("ticks"));

        // 1. Fire before_action synchronously before the loop begins.
        if (data.isPresent("before_action")) {
            ActionFactory<T>.Instance beforeAction = data.get("before_action");
            beforeAction.accept(t);
        }

        // 2. Schedule each iteration of the loop body. Iteration I fire at (I * ticks) ticks from now,
        // so the first iteration fires after 'ticks' ticks, the second after '2 * ticks', and so on.
        if (data.isPresent("action")) {
            ActionFactory<T>.Instance loopAction = data.get("action");
            for (int i = 1; i <= value; i++) {
                final int delay = i * ticks;
                SCHEDULER.queue(server -> loopAction.accept(t), delay);
            }
        }

        // 3. Schedule after_action to coincide with the tick of the final iteration. If value == 0 this resolves to delay 0,
        // i.e. the end of the current tick, which is the correct "loop finished immediately" behavior.
        //
        // Both the last action and after_action are queued into the same ArrayList for that tick,
        // so they are processed in insertion order: action first, after_action second.
        if (data.isPresent("after_action")) {
            ActionFactory<T>.Instance afterAction = data.get("after_action");
            SCHEDULER.queue(server -> afterAction.accept(t), value * ticks);
        }
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------
    public static <T> ActionFactory<T> getFactory(
            SerializableDataType<ActionFactory<T>.Instance> dataType) {

        return new ActionFactory<>(
                Sync.identifier("loop"),
                new SerializableData()
                        // How many times the loop body should execute.
                        .add("value", SerializableDataTypes.INT, 1)
                        // Minimum 1 tick between iterations to avoid stacking.
                        .add("ticks", SerializableDataTypes.INT, 1)
                        // Fired once, immediately, before iteration 1.
                        .add("before_action", dataType, null)
                        // Fired once per iteration.
                        .add("action", dataType, null)
                        // Fired once after the final iteration.
                        .add("after_action", dataType, null),
                LoopAction::action
        );
    }
}