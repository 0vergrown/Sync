package dev.overgrown.sync.factory.action.entity;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.save_location_and_teleport_to_location.EntityLocationsState;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SaveLocationAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        String id = data.getString("id");
        boolean overwrite = data.getBoolean("overwrite");

        // Determine if this entity should be considered persistent
        boolean isPersistent = isEntityPersistent(entity);

        EntityLocationsState state = EntityLocationsState.get(serverWorld);
        state.saveLocation(
                entity.getUuid(),
                id,
                entity.getPos(),
                entity.getWorld().getRegistryKey(),
                entity.getYaw(),
                entity.getPitch(),
                overwrite,
                isPersistent
        );
    }

    private static boolean isEntityPersistent(Entity entity) {
        // Players are always persistent
        if (entity instanceof ServerPlayerEntity) {
            return true;
        }

        // Name-tagged entities are persistent
        if (entity.hasCustomName()) {
            return true;
        }

        // PathAwareEntity mobs are considered persistent by default
        // This includes most mobs that can have AI behaviors
        if (entity instanceof PathAwareEntity) {
            return true;
        }

        // For other entities, check for additional persistence flags like if entities that are explicitly marked as persistent in some way
        // Most other entities (items, projectiles, etc.) are not persistent
        return false;
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("save_location"),
                new SerializableData()
                        .add("id", SerializableDataTypes.STRING)
                        .add("overwrite", SerializableDataTypes.BOOLEAN, true),
                SaveLocationAction::action
        );
    }
}