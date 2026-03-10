package dev.overgrown.sync.factory.action.entity.use_selected_stolen_power;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.bientity.transfer.StolenPowerSlotManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

import java.util.List;

public class UseSelectedStolenPowerAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity.getWorld().isClient()) return;

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (component == null) return;

        List<PowerType<?>> powers = StolenPowerSlotManager.getSelectedPackagePowers(entity);
        if (powers.isEmpty()) return;

        for (PowerType<?> pt : powers) {
            Power power = component.getPower(pt);
            if (power instanceof ActiveCooldownPower acp) {
                acp.onUse();
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("use_selected_stolen_power"),
                new SerializableData(),
                UseSelectedStolenPowerAction::action
        );
    }
}
