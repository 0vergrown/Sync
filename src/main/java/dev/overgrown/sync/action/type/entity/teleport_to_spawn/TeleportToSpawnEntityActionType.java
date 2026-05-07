package dev.overgrown.sync.action.type.entity.teleport_to_spawn;

import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class TeleportToSpawnEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<TeleportToSpawnEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("player_spawn", SerializableDataTypes.BOOLEAN, false),
        data -> new TeleportToSpawnEntityActionType(data.get("player_spawn")),
        (actionType, serializableData) -> serializableData.instance()
            .set("player_spawn", actionType.usePlayerSpawn)
    );

    private final boolean usePlayerSpawn;

    public TeleportToSpawnEntityActionType(boolean usePlayerSpawn) {
        this.usePlayerSpawn = usePlayerSpawn;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (!(context.entity() instanceof ServerPlayerEntity player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerWorld targetWorld;
        BlockPos targetPos;
        float targetYaw;

        if (usePlayerSpawn) {
            BlockPos spawnPos = player.getSpawnPointPosition();
            RegistryKey<World> spawnDim = player.getSpawnPointDimension();
            ServerWorld spawnWorld = (spawnPos != null && spawnDim != null) ? server.getWorld(spawnDim) : null;

            if (spawnWorld != null) {
                targetWorld = spawnWorld;
                targetPos = spawnPos;
                targetYaw = player.getSpawnAngle();
            } else {
                targetWorld = server.getOverworld();
                targetPos = targetWorld.getSpawnPos();
                targetYaw = targetWorld.getSpawnAngle();
            }
        } else {
            targetWorld = server.getOverworld();
            targetPos = targetWorld.getSpawnPos();
            targetYaw = targetWorld.getSpawnAngle();
        }

        player.teleport(
            targetWorld,
            targetPos.getX() + 0.5,
            targetPos.getY(),
            targetPos.getZ() + 0.5,
            targetYaw,
            0.0F
        );
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.TELEPORT_TO_SPAWN;
    }
}
