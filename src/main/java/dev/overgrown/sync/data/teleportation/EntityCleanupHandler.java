package dev.overgrown.sync.data.teleportation;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class EntityCleanupHandler {

    private EntityCleanupHandler() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
            if (entity instanceof ServerPlayerEntity) return;

            if (shouldRemoveEntityData(entity)) {
                EntityLocationsState state = EntityLocationsState.get(serverWorld);
                state.removeAllLocationsForEntity(entity.getUuid());
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity) return;

            if (entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
                if (shouldRemoveEntityData(entity)) {
                    EntityLocationsState state = EntityLocationsState.get(world);
                    state.removeAllLocationsForEntity(entity.getUuid());
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                EntityLocationsState state = EntityLocationsState.get(world);
                state.cleanupLocations(server);
            }
        });
    }

    private static boolean shouldRemoveEntityData(Entity entity) {
        return !(entity instanceof ServerPlayerEntity
            || entity.hasCustomName()
            || entity instanceof PathAwareEntity);
    }
}
