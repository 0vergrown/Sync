package dev.overgrown.sync.factory.action.bientity.save_location;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.teleportation.SaveLocationAction;
import dev.overgrown.sync.factory.action.entity.teleportation.data.EntityLocationsState;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;

/**
 * Bientity action that lets the actor save the target's location under a given id.
 *
 * <p>If {@code track_entity} is true, the saved location follows the target across
 * future ticks (resolved at teleport time); otherwise the target's current position
 * is frozen as a static location. Useful for composing with raycast bientity_action,
 * collisions, or any other actor+target context.</p>
 */
public class SaveLocationBientityAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> pair) {
        Entity actor = pair.getLeft();
        Entity target = pair.getRight();

        if (!(actor.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        String id = data.getString("id");
        boolean overwrite = data.getBoolean("overwrite");
        boolean trackEntity = data.getBoolean("track_entity");

        boolean isPersistent = SaveLocationAction.isEntityPersistent(actor);
        EntityLocationsState state = EntityLocationsState.get(serverWorld.getServer());

        if (trackEntity) {
            state.saveTrackedLocation(
                    actor.getUuid(),
                    id,
                    target.getUuid(),
                    target.getPos(),
                    target.getWorld().getRegistryKey(),
                    target.getYaw(),
                    target.getPitch(),
                    overwrite,
                    isPersistent
            );
        } else {
            state.saveStaticLocation(
                    actor.getUuid(),
                    id,
                    target.getPos(),
                    target.getWorld().getRegistryKey(),
                    target.getYaw(),
                    target.getPitch(),
                    overwrite,
                    isPersistent
            );
        }
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("save_location"),
                new SerializableData()
                        .add("id", SerializableDataTypes.STRING)
                        .add("overwrite", SerializableDataTypes.BOOLEAN, true)
                        .add("track_entity", SerializableDataTypes.BOOLEAN, false),
                SaveLocationBientityAction::action
        );
    }
}
