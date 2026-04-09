package dev.overgrown.sync.factory.action.bientity.disguise;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.disguise.DisguiseManager;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

public class DisguiseAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor  = pair.getLeft();
        Entity target = pair.getRight();

        if (!(actor instanceof LivingEntity livingActor)) {
            Sync.LOGGER.warn("sync:disguise – actor '{}' is not a LivingEntity, skipping.",
                    actor.getType().getUntranslatedName());
            return;
        }

        boolean overwrite = data.getBoolean("overwrite");
        DisguiseManager.applyDisguise(livingActor, target, overwrite);
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("disguise"),
                new SerializableData()
                        .add("overwrite", SerializableDataTypes.BOOLEAN, true),
                DisguiseAction::action
        );
    }
}