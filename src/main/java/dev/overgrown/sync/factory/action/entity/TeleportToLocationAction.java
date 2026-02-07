package dev.overgrown.sync.factory.action.entity;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.SavedLocationsState;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class TeleportToLocationAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        String id = data.getString("id");

        SavedLocationsState state = SavedLocationsState.get(serverWorld);
        SavedLocationsState.SavedLocation savedLocation = state.getLocation(player.getUuid(), id);

        if (savedLocation == null) {
            return;
        }

        MinecraftServer server = serverWorld.getServer();
        ServerWorld targetWorld = server.getWorld(savedLocation.dimension());

        if (targetWorld == null) {
            return;
        }

        // Teleport the player to the saved location
        player.teleport(
                targetWorld,
                savedLocation.position().x,
                savedLocation.position().y,
                savedLocation.position().z,
                savedLocation.yaw(),
                savedLocation.pitch()
        );
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