package dev.overgrown.sync.action.type.entity.meta;

import dev.overgrown.sync.action.type.meta.loop.LoopMetaActionType;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LoopEntityActionType extends EntityActionType implements LoopMetaActionType<EntityActionContext, EntityAction> {

    private final int value;
    private final int ticks;
    private final Optional<EntityAction> beforeAction;
    private final Optional<EntityAction> action;
    private final Optional<EntityAction> afterAction;

    public LoopEntityActionType(int value, int ticks,
                                Optional<EntityAction> beforeAction,
                                Optional<EntityAction> action,
                                Optional<EntityAction> afterAction) {
        this.value = value;
        this.ticks = ticks;
        this.beforeAction = beforeAction;
        this.action = action;
        this.afterAction = afterAction;
    }

    @Override
    public void accept(EntityActionContext context) {
        executeAction(context);
    }

    @Override
    public int value() { return value; }

    @Override
    public int ticks() { return ticks; }

    @Override
    public Optional<EntityAction> beforeAction() { return beforeAction; }

    @Override
    public Optional<EntityAction> action() { return action; }

    @Override
    public Optional<EntityAction> afterAction() { return afterAction; }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.LOOP;
    }
}
