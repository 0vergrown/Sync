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

public class UseSelectedStolenPowerAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity.getWorld().isClient()) return;

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (component == null) return;

        PowerType<?> selected = StolenPowerSlotManager.getSelectedPowerInPackage(entity);
        if (selected == null) {
            Sync.LOGGER.debug("[Sync/UseSelected] {} has no selected stolen package or it is empty.",
                    entity.getEntityName());
            return;
        }

        Power power = component.getPower(selected);
        if (power == null) {
            Sync.LOGGER.debug("[Sync/UseSelected] {} holds {} in slot but getPower returned null.",
                    entity.getEntityName(), selected.getIdentifier());
            return;
        }

        if (power instanceof ActiveCooldownPower acp) {
            acp.onUse();
            Sync.LOGGER.debug("[Sync/UseSelected] {} used {} via slot activation.",
                    entity.getEntityName(), selected.getIdentifier());
        } else {
            Sync.LOGGER.debug("[Sync/UseSelected] Selected power {} on {} is not an ActiveCooldownPower ({}); skipping.",
                    selected.getIdentifier(), entity.getEntityName(), power.getClass().getSimpleName());
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