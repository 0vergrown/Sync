package dev.overgrown.sync.factory.action.entity;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.SavedLocationsState;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SaveLocationAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        String id = data.getString("id");
        boolean overwrite = data.getBoolean("overwrite");

        SavedLocationsState state = SavedLocationsState.get(serverWorld);
        state.saveLocation(
                player.getUuid(),
                id,
                entity.getPos(),
                entity.getWorld().getRegistryKey(),
                entity.getYaw(),
                entity.getPitch(),
                overwrite
        );
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