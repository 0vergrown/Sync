package dev.overgrown.sync.factory.action.entity.teleportation;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportToSpawnAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        // Only players have spawn points
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        boolean usePlayerSpawn = data.getBoolean("player_spawn");
        ServerWorld targetWorld;
        BlockPos targetPos;
        float targetYaw;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (usePlayerSpawn) {
            // Try to get player's personal spawn point
            BlockPos spawnPos = player.getSpawnPointPosition();
            RegistryKey<World> spawnDim = player.getSpawnPointDimension();
            if (spawnPos != null && spawnDim != null) {
                ServerWorld spawnWorld = server.getWorld(spawnDim);
                if (spawnWorld != null) {
                    targetWorld = spawnWorld;
                    targetPos = spawnPos;
                    targetYaw = player.getSpawnAngle();
                } else {
                    // Fallback to world spawn if dimension isn't loaded
                    targetWorld = server.getOverworld();
                    targetPos = targetWorld.getSpawnPos();
                    targetYaw = targetWorld.getSpawnAngle();
                }
            } else {
                // No spawn point set → use world spawn
                targetWorld = server.getOverworld();
                targetPos = targetWorld.getSpawnPos();
                targetYaw = targetWorld.getSpawnAngle();
            }
        } else {
            // World spawn is always in the overworld
            targetWorld = server.getOverworld();
            targetPos = targetWorld.getSpawnPos();
            targetYaw = targetWorld.getSpawnAngle();
        }

        // Teleport the player
        player.teleport(
                targetWorld,
                targetPos.getX() + 0.5, // center of block
                targetPos.getY(),
                targetPos.getZ() + 0.5,
                targetYaw,
                0.0F // pitch (spawn points don't store pitch)
        );
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("teleport_to_spawn"),
                new SerializableData()
                        .add("player_spawn", SerializableDataTypes.BOOLEAN, false),
                TeleportToSpawnAction::action
        );
    }
}