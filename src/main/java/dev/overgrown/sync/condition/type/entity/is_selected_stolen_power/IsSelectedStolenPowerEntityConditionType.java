package dev.overgrown.sync.condition.type.entity.is_selected_stolen_power;

import dev.overgrown.sync.data.transfer.StolenPowerSlotManager;
import dev.overgrown.sync.registry.SyncEntityConditionTypes;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class IsSelectedStolenPowerEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<IsSelectedStolenPowerEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("source", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty()),
        data -> new IsSelectedStolenPowerEntityConditionType(data.get("source")),
        (conditionType, serializableData) -> serializableData.instance()
            .set("source", conditionType.source)
    );

    private final Optional<Identifier> source;

    public IsSelectedStolenPowerEntityConditionType(Optional<Identifier> source) {
        this.source = source;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        if (source.isPresent()) {
            return StolenPowerSlotManager.isSourceSelected(context.entity(), source.get());
        }
        return StolenPowerSlotManager.getSelectedSource(context.entity()) != null;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return SyncEntityConditionTypes.IS_SELECTED_STOLEN_POWER;
    }
}
