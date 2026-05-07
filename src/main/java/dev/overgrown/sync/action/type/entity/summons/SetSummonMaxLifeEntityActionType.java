package dev.overgrown.sync.action.type.entity.summons;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.action.type.entity.summons.utils.Temporary;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

public class SetSummonMaxLifeEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SetSummonMaxLifeEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("amount", SerializableDataTypes.INT),
        data -> new SetSummonMaxLifeEntityActionType(data.get("amount")),
        (a, sd) -> sd.instance().set("amount", a.amount)
    );

    private final int amount;

    public SetSummonMaxLifeEntityActionType(int amount) {
        this.amount = amount;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (context.entity() instanceof Temporary summonable) {
            summonable.setMaxLifetime(amount);
        } else {
            Sync.LOGGER.warn("Attempted to use set_summon_max_life_ticks action on a non-temporary entity.");
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.SET_SUMMON_MAX_LIFE_TICKS;
    }
}
