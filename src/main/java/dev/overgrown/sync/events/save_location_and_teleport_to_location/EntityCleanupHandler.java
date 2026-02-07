package dev.overgrown.sync.events.save_location_and_teleport_to_location;

import dev.overgrown.sync.data.save_location_and_teleport_to_location.EntityLocationsState;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class EntityCleanupHandler {

    public static void register() {
        // Clean up locations when entities die
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Direct cast to ServerWorld since we know it's server-side
            ServerWorld serverWorld = (ServerWorld) entity.getWorld();

            // Don't clean up player data on death (players respawn)
            if (entity instanceof ServerPlayerEntity) {
                return;
            }

            // Only cleanup entities whose data should be removed
            if (shouldRemoveEntityData(entity)) {
                EntityLocationsState state = EntityLocationsState.get(serverWorld);
                state.removeAllLocationsForEntity(entity.getUuid());
            }
        });

        // Clean up locations when entities are unloaded/removed
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            ServerWorld serverWorld = (ServerWorld) world;

            // Don't clean up player data
            if (entity instanceof ServerPlayerEntity) {
                return;
            }

            // Only cleanup if entity is dead or shouldn't be persistent
            if (entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
                if (shouldRemoveEntityData(entity)) {
                    EntityLocationsState state = EntityLocationsState.get(serverWorld);
                    state.removeAllLocationsForEntity(entity.getUuid());
                }
            }
        });

        // Periodic cleanup for entities that might have been removed without events
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Run cleanup on all worlds
            for (ServerWorld world : server.getWorlds()) {
                EntityLocationsState state = EntityLocationsState.get(world);
                state.cleanupLocations(server);
            }
        });
    }

    private static boolean shouldRemoveEntityData(Entity entity) {
        // Remove data for entities that are not players, not name-tagged, and not PathAwareEntity
        return !(entity instanceof ServerPlayerEntity ||
                entity.hasCustomName() ||
                entity instanceof PathAwareEntity);
    }
}