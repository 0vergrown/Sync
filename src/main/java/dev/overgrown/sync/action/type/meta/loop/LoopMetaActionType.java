package dev.overgrown.sync.action.type.meta.loop;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.action.Action;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.util.Scheduler;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.Optional;
import java.util.function.Function;

/**
 * Generic meta action that runs an action {@code value} times spaced by
 * {@code ticks} ticks. Supports optional {@code before_action} and
 * {@code after_action} that fire once each.
 */
public interface LoopMetaActionType<T extends ActionContext<?>, A extends Action<T, ? extends ActionType<T, A>>> {

    Scheduler SCHEDULER = new Scheduler();

    int value();

    int ticks();

    Optional<A> beforeAction();

    Optional<A> action();

    Optional<A> afterAction();

    default void executeAction(T context) {
        int v = Math.max(0, value());
        int t = Math.max(1, ticks());

        beforeAction().ifPresent(a -> a.accept(context));

        action().ifPresent(loop -> {
            for (int i = 1; i <= v; i++) {
                final int delay = i * t;
                SCHEDULER.queue(server -> loop.accept(context), delay);
            }
        });

        afterAction().ifPresent(after -> SCHEDULER.queue(server -> after.accept(context), v * t));
    }

    static <T extends ActionContext<?>, A extends Action<T, AT>, AT extends ActionType<T, A>, M extends ActionType<T, A> & LoopMetaActionType<T, A>>
    ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, LoopFactory<A, M> constructor) {
        return ActionConfiguration.of(
            Sync.identifier("loop"),
            new SerializableData()
                .add("value", SerializableDataTypes.INT, 1)
                .add("ticks", SerializableDataTypes.INT, 1)
                .add("before_action", actionDataType.optional(), Optional.empty())
                .add("action", actionDataType.optional(), Optional.empty())
                .add("after_action", actionDataType.optional(), Optional.empty()),
            data -> constructor.create(
                data.get("value"),
                data.get("ticks"),
                data.get("before_action"),
                data.get("action"),
                data.get("after_action")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("value", m.value())
                .set("ticks", m.ticks())
                .set("before_action", m.beforeAction())
                .set("action", m.action())
                .set("after_action", m.afterAction())
        );
    }

    @FunctionalInterface
    interface LoopFactory<A, M> {
        M create(int value, int ticks, Optional<A> before, Optional<A> action, Optional<A> after);
    }
}
