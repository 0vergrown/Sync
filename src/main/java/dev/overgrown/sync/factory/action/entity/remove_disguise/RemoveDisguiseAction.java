package dev.overgrown.sync.factory.action.entity.remove_disguise;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.disguise.DisguiseManager;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class RemoveDisguiseAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof LivingEntity living) {
            DisguiseManager.removeDisguise(living);
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("remove_disguise"),
                new SerializableData(),
                RemoveDisguiseAction::action
        );
    }
}