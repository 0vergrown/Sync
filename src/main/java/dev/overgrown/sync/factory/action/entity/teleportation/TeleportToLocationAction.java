package dev.overgrown.sync.factory.action.entity.teleportation;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.teleportation.data.EntityLocationsState;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class TeleportToLocationAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        String id = data.getString("id");

        EntityLocationsState state = EntityLocationsState.get(serverWorld);
        EntityLocationsState.SavedLocation savedLocation = state.getLocation(entity.getUuid(), id);

        if (savedLocation == null) {
            return;
        }

        MinecraftServer server = serverWorld.getServer();
        ServerWorld targetWorld = server.getWorld(savedLocation.dimension());

        if (targetWorld == null) {
            return;
        }

        // Handle teleportation differently for players vs other entities
        if (entity instanceof ServerPlayerEntity player) {
            // Player teleportation with rotation
            player.teleport(
                    targetWorld,
                    savedLocation.position().x,
                    savedLocation.position().y,
                    savedLocation.position().z,
                    savedLocation.yaw(),
                    savedLocation.pitch()
            );
        } else {
            // Non-player entity teleportation
            Entity teleportedEntity = entity;

            // If changing dimensions, use moveToWorld
            if (!entity.getWorld().getRegistryKey().equals(savedLocation.dimension())) {
                teleportedEntity = entity.moveToWorld(targetWorld);
                if (teleportedEntity == null) {
                    return; // Failed to move between dimensions
                }
            }

            // Set position and rotation
            teleportedEntity.refreshPositionAndAngles(
                    savedLocation.position().x,
                    savedLocation.position().y,
                    savedLocation.position().z,
                    savedLocation.yaw(),
                    savedLocation.pitch()
            );

            // For non-player entities, also set head yaw
            teleportedEntity.setHeadYaw(savedLocation.yaw());

            // Stop riding if entity is riding something
            teleportedEntity.stopRiding();

            // For mobs, stop their navigation
            if (teleportedEntity instanceof net.minecraft.entity.mob.PathAwareEntity pathAwareEntity) {
                pathAwareEntity.getNavigation().stop();
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("teleport_to_location"),
                new SerializableData()
                        .add("id", SerializableDataTypes.STRING),
                TeleportToLocationAction::action
        );
    }
}