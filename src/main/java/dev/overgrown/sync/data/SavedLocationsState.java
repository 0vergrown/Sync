package dev.overgrown.sync.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SavedLocationsState extends PersistentState {
    private static final String STATE_NAME = "sync_saved_locations";

    private final Map<UUID, Map<String, SavedLocation>> playerLocations = new HashMap<>();

    public SavedLocationsState() {
        super();
    }

    public static SavedLocationsState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                SavedLocationsState::fromNbt,
                SavedLocationsState::new,
                STATE_NAME
        );
    }

    public void saveLocation(UUID playerId, String id, Vec3d position, RegistryKey<World> dimension, float yaw, float pitch, boolean overwrite) {
        Map<String, SavedLocation> playerMap = playerLocations.computeIfAbsent(playerId, k -> new HashMap<>());

        if (!overwrite && playerMap.containsKey(id)) {
            return; // Don't overwrite if overwrite is false
        }

        playerMap.put(id, new SavedLocation(position, dimension, yaw, pitch));
        markDirty();
    }

    @Nullable
    public SavedLocation getLocation(UUID playerId, String id) {
        Map<String, SavedLocation> playerMap = playerLocations.get(playerId);
        if (playerMap == null) {
            return null;
        }
        return playerMap.get(id);
    }

    public boolean removeLocation(UUID playerId, String id) {
        Map<String, SavedLocation> playerMap = playerLocations.get(playerId);
        if (playerMap == null) {
            return false;
        }

        boolean removed = playerMap.remove(id) != null;
        if (removed) {
            markDirty();
        }
        return removed;
    }

    public static SavedLocationsState fromNbt(NbtCompound nbt) {
        SavedLocationsState state = new SavedLocationsState();
        NbtList playersList = nbt.getList("players", NbtElement.COMPOUND_TYPE);

        for (NbtElement playerElement : playersList) {
            NbtCompound playerCompound = (NbtCompound) playerElement;
            UUID playerId = playerCompound.getUuid("playerId");
            NbtList locationsList = playerCompound.getList("locations", NbtElement.COMPOUND_TYPE);

            Map<String, SavedLocation> playerMap = new HashMap<>();
            for (NbtElement locationElement : locationsList) {
                NbtCompound locationCompound = (NbtCompound) locationElement;
                String id = locationCompound.getString("id");
                Vec3d position = new Vec3d(
                        locationCompound.getDouble("x"),
                        locationCompound.getDouble("y"),
                        locationCompound.getDouble("z")
                );
                RegistryKey<World> dimension = World.OVERWORLD;
                if (locationCompound.contains("dimension")) {
                    dimension = RegistryKey.of(RegistryKeys.WORLD, new Identifier(locationCompound.getString("dimension")));
                }
                float yaw = locationCompound.getFloat("yaw");
                float pitch = locationCompound.getFloat("pitch");

                playerMap.put(id, new SavedLocation(position, dimension, yaw, pitch));
            }

            state.playerLocations.put(playerId, playerMap);
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList playersList = new NbtList();

        for (Map.Entry<UUID, Map<String, SavedLocation>> playerEntry : playerLocations.entrySet()) {
            NbtCompound playerCompound = new NbtCompound();
            playerCompound.putUuid("playerId", playerEntry.getKey());

            NbtList locationsList = new NbtList();
            for (Map.Entry<String, SavedLocation> locationEntry : playerEntry.getValue().entrySet()) {
                NbtCompound locationCompound = new NbtCompound();
                locationCompound.putString("id", locationEntry.getKey());
                locationCompound.putDouble("x", locationEntry.getValue().position().x);
                locationCompound.putDouble("y", locationEntry.getValue().position().y);
                locationCompound.putDouble("z", locationEntry.getValue().position().z);
                locationCompound.putString("dimension", locationEntry.getValue().dimension().getValue().toString());
                locationCompound.putFloat("yaw", locationEntry.getValue().yaw());
                locationCompound.putFloat("pitch", locationEntry.getValue().pitch());

                locationsList.add(locationCompound);
            }

            playerCompound.put("locations", locationsList);
            playersList.add(playerCompound);
        }

        nbt.put("players", playersList);
        return nbt;
    }

    public record SavedLocation(Vec3d position, RegistryKey<World> dimension, float yaw, float pitch) {}
}