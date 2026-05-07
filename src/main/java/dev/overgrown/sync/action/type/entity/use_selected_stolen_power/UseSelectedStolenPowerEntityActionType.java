package dev.overgrown.sync.action.type.entity.use_selected_stolen_power;

import dev.overgrown.sync.data.transfer.StolenPowerSlotManager;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.ActiveCooldownPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UseSelectedStolenPowerEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<UseSelectedStolenPowerEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData(),
        data -> new UseSelectedStolenPowerEntityActionType(),
        (actionType, serializableData) -> serializableData.instance()
    );

    @Override
    public void accept(EntityActionContext context) {
        if (context.entity().getWorld().isClient()) return;

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(context.entity()).orElse(null);
        if (component == null) return;

        List<Power> powers = StolenPowerSlotManager.getSelectedPackagePowers(context.entity());
        if (powers.isEmpty()) return;

        for (Power power : powers) {
            PowerType pt = component.getPowerType(power);
            if (pt instanceof ActiveCooldownPowerType acp) {
                acp.use();
            }
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.USE_SELECTED_STOLEN_POWER;
    }
}
