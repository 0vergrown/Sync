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

public class EntityLocationsState extends PersistentState {
    private static final String STATE_NAME = "sync_entity_locations";

    private final Map<UUID, Map<String, SavedLocation>> entityLocations = new HashMap<>();

    public EntityLocationsState() {
        super();
    }

    public static EntityLocationsState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                EntityLocationsState::fromNbt,
                EntityLocationsState::new,
                STATE_NAME
        );
    }

    public void saveLocation(UUID entityId, String id, Vec3d position, RegistryKey<World> dimension, float yaw, float pitch, boolean overwrite) {
        Map<String, SavedLocation> entityMap = entityLocations.computeIfAbsent(entityId, k -> new HashMap<>());

        if (!overwrite && entityMap.containsKey(id)) {
            return; // Don't overwrite if overwrite is false
        }

        entityMap.put(id, new SavedLocation(position, dimension, yaw, pitch));
        markDirty();
    }

    @Nullable
    public SavedLocation getLocation(UUID entityId, String id) {
        Map<String, SavedLocation> entityMap = entityLocations.get(entityId);
        if (entityMap == null) {
            return null;
        }
        return entityMap.get(id);
    }

    public boolean removeLocation(UUID entityId, String id) {
        Map<String, SavedLocation> entityMap = entityLocations.get(entityId);
        if (entityMap == null) {
            return false;
        }

        boolean removed = entityMap.remove(id) != null;
        if (removed) {
            markDirty();
        }
        return removed;
    }

    public static EntityLocationsState fromNbt(NbtCompound nbt) {
        EntityLocationsState state = new EntityLocationsState();
        NbtList entitiesList = nbt.getList("entities", NbtElement.COMPOUND_TYPE);

        for (NbtElement entityElement : entitiesList) {
            NbtCompound entityCompound = (NbtCompound) entityElement;
            UUID entityId = entityCompound.getUuid("entityId");
            NbtList locationsList = entityCompound.getList("locations", NbtElement.COMPOUND_TYPE);

            Map<String, SavedLocation> entityMap = new HashMap<>();
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

                entityMap.put(id, new SavedLocation(position, dimension, yaw, pitch));
            }

            state.entityLocations.put(entityId, entityMap);
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList entitiesList = new NbtList();

        for (Map.Entry<UUID, Map<String, SavedLocation>> entityEntry : entityLocations.entrySet()) {
            NbtCompound entityCompound = new NbtCompound();
            entityCompound.putUuid("entityId", entityEntry.getKey());

            NbtList locationsList = new NbtList();
            for (Map.Entry<String, SavedLocation> locationEntry : entityEntry.getValue().entrySet()) {
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

            entityCompound.put("locations", locationsList);
            entitiesList.add(entityCompound);
        }

        nbt.put("entities", entitiesList);
        return nbt;
    }

    public record SavedLocation(Vec3d position, RegistryKey<World> dimension, float yaw, float pitch) {}
}