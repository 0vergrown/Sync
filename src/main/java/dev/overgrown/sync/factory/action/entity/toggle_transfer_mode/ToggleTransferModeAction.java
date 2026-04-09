package dev.overgrown.sync.factory.action.entity.toggle_transfer_mode;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.toggle_transfer_mode.utils.TransferModeManager;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class ToggleTransferModeAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity.getWorld().isClient()) return;
        TransferModeManager.toggle(entity);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("toggle_transfer_mode"),
                new SerializableData(),
                ToggleTransferModeAction::action
        );
    }
}