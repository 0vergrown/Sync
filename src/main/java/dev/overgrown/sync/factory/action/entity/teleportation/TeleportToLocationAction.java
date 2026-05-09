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
        MinecraftServer server = serverWorld.getServer();

        EntityLocationsState state = EntityLocationsState.get(server);
        EntityLocationsState.ResolvedLocation resolved = state.resolve(server, entity.getUuid(), id);

        if (resolved == null) {
            return;
        }

        ServerWorld targetWorld = server.getWorld(resolved.dimension());
        if (targetWorld == null) {
            return;
        }

        if (entity instanceof ServerPlayerEntity player) {
            player.teleport(
                    targetWorld,
                    resolved.position().x,
                    resolved.position().y,
                    resolved.position().z,
                    resolved.yaw(),
                    resolved.pitch()
            );
        } else {
            Entity teleportedEntity = entity;

            if (!entity.getWorld().getRegistryKey().equals(resolved.dimension())) {
                teleportedEntity = entity.moveToWorld(targetWorld);
                if (teleportedEntity == null) {
                    return;
                }
            }

            teleportedEntity.refreshPositionAndAngles(
                    resolved.position().x,
                    resolved.position().y,
                    resolved.position().z,
                    resolved.yaw(),
                    resolved.pitch()
            );
            teleportedEntity.setHeadYaw(resolved.yaw());
            teleportedEntity.stopRiding();

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