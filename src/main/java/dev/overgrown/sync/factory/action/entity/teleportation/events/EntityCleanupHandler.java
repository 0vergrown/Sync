package dev.overgrown.sync.factory.action.entity.teleportation.events;

import dev.overgrown.sync.factory.action.entity.teleportation.data.EntityLocationsState;
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
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            ServerWorld serverWorld = (ServerWorld) entity.getWorld();

            if (entity instanceof ServerPlayerEntity) {
                return;
            }

            if (shouldRemoveEntityData(entity)) {
                EntityLocationsState state = EntityLocationsState.get(serverWorld.getServer());
                state.removeAllLocationsForEntity(entity.getUuid());
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            ServerWorld serverWorld = (ServerWorld) world;

            if (entity instanceof ServerPlayerEntity) {
                return;
            }

            if (entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
                if (shouldRemoveEntityData(entity)) {
                    EntityLocationsState state = EntityLocationsState.get(serverWorld.getServer());
                    state.removeAllLocationsForEntity(entity.getUuid());
                }
            }
        });

        // State is server-global (anchored on overworld), so we cleanup once per tick.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EntityLocationsState state = EntityLocationsState.get(server);
            state.cleanupLocations(server);
        });
    }

    private static boolean shouldRemoveEntityData(Entity entity) {
        return !(entity instanceof ServerPlayerEntity
                || entity.hasCustomName()
                || entity instanceof PathAwareEntity);
    }
}