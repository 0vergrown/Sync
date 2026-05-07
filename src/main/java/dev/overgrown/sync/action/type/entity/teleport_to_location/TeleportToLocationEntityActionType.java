package dev.overgrown.sync.action.type.entity.teleport_to_location;

import dev.overgrown.sync.data.teleportation.EntityLocationsState;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public class TeleportToLocationEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<TeleportToLocationEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("id", SerializableDataTypes.STRING),
        data -> new TeleportToLocationEntityActionType(data.get("id")),
        (actionType, serializableData) -> serializableData.instance()
            .set("id", actionType.id)
    );

    private final String id;

    public TeleportToLocationEntityActionType(String id) {
        this.id = id;
    }

    @Override
    public void accept(EntityActionContext context) {
        Entity entity = context.entity();
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;

        EntityLocationsState state = EntityLocationsState.get(serverWorld);
        EntityLocationsState.SavedLocation saved = state.getLocation(entity.getUuid(), id);
        if (saved == null) return;

        MinecraftServer server = serverWorld.getServer();
        ServerWorld targetWorld = server.getWorld(saved.dimension());
        if (targetWorld == null) return;

        if (entity instanceof ServerPlayerEntity player) {
            player.teleport(
                targetWorld,
                saved.position().x,
                saved.position().y,
                saved.position().z,
                saved.yaw(),
                saved.pitch()
            );
            return;
        }

        // Same-dimension non-player entity teleport
        if (entity.getWorld().getRegistryKey().equals(saved.dimension())) {
            entity.refreshPositionAndAngles(
                saved.position().x,
                saved.position().y,
                saved.position().z,
                saved.yaw(),
                saved.pitch()
            );
            entity.setHeadYaw(saved.yaw());
            entity.stopRiding();
            if (entity instanceof PathAwareEntity pathAware) {
                pathAware.getNavigation().stop();
            }
        }
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.TELEPORT_TO_LOCATION;
    }
}
