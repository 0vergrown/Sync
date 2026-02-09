package dev.overgrown.sync.factory.action.entity.teleportation.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityLocationsState extends PersistentState {
    private static final String STATE_NAME = "sync_entity_locations";

    // Use ConcurrentHashMap for thread safety
    private final Map<UUID, EntityLocationData> entityLocations = new ConcurrentHashMap<>();

    // Track when locations were last accessed for cleanup
    private final Map<UUID, Long> lastAccessTime = new ConcurrentHashMap<>();

    // Configurable cleanup settings (in ticks, 20 ticks = 1 second)
    private static final long CLEANUP_INTERVAL = 20 * 60 * 10; // 10 minutes
    private static final long MAX_INACTIVE_TIME = 20 * 60 * 30; // 30 minutes for non-persistent entities
    private long lastCleanupTime = 0;

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

    public void saveLocation(UUID entityId, String id, Vec3d position, RegistryKey<World> dimension,
                             float yaw, float pitch, boolean overwrite, boolean isPersistent) {
        EntityLocationData entityData = entityLocations.computeIfAbsent(entityId,
                k -> new EntityLocationData(isPersistent));

        if (!overwrite && entityData.locations.containsKey(id)) {
            return; // Don't overwrite if overwrite is false
        }

        entityData.locations.put(id, new SavedLocation(position, dimension, yaw, pitch));
        lastAccessTime.put(entityId, System.currentTimeMillis());
        markDirty();
    }

    @Nullable
    public SavedLocation getLocation(UUID entityId, String id) {
        EntityLocationData entityData = entityLocations.get(entityId);
        if (entityData == null) {
            return null;
        }

        // Update last access time
        lastAccessTime.put(entityId, System.currentTimeMillis());
        return entityData.locations.get(id);
    }

    public boolean removeLocation(UUID entityId, String id) {
        EntityLocationData entityData = entityLocations.get(entityId);
        if (entityData == null) {
            return false;
        }

        boolean removed = entityData.locations.remove(id) != null;
        if (removed) {
            markDirty();
        }
        return removed;
    }

    public void removeAllLocationsForEntity(UUID entityId) {
        if (entityLocations.remove(entityId) != null) {
            lastAccessTime.remove(entityId);
            markDirty();
        }
    }

    /**
     * Periodic cleanup to remove locations for entities that no longer exist
     */
    public void cleanupLocations(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();

        // Only run cleanup every CLEANUP_INTERVAL
        if (currentTime - lastCleanupTime < CLEANUP_INTERVAL * 50) { // Convert ticks to ms
            return;
        }

        lastCleanupTime = currentTime;
        boolean changed = false;

        Iterator<Map.Entry<UUID, EntityLocationData>> iterator = entityLocations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, EntityLocationData> entry = iterator.next();
            UUID entityId = entry.getKey();
            EntityLocationData data = entry.getValue();

            // Always keep player data (players are persistent)
            if (isPlayer(entityId, server)) {
                continue;
            }

            // For non-persistent entities, check if they're still alive
            if (!data.isPersistent) {
                boolean entityExists = false;

                // Check all worlds for this entity
                for (ServerWorld world : server.getWorlds()) {
                    Entity entity = world.getEntity(entityId);
                    if (entity != null && entity.isAlive()) {
                        entityExists = true;

                        // Check if this entity should be considered persistent now
                        // (e.g., if it was name-tagged after saving location)
                        if (entity.hasCustomName() || entity instanceof PathAwareEntity) {
                            data.isPersistent = true;
                        }
                        break;
                    }
                }

                // If entity doesn't exist and hasn't been accessed recently, remove it
                if (!entityExists) {
                    Long lastAccess = lastAccessTime.get(entityId);
                    if (lastAccess == null ||
                            currentTime - lastAccess > MAX_INACTIVE_TIME * 50) {
                        iterator.remove();
                        lastAccessTime.remove(entityId);
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            markDirty();
        }
    }

    private boolean isPlayer(UUID uuid, MinecraftServer server) {
        // Check if this UUID belongs to a player
        return server.getPlayerManager().getPlayer(uuid) != null;
    }

    public static EntityLocationsState fromNbt(NbtCompound nbt) {
        EntityLocationsState state = new EntityLocationsState();
        NbtList entitiesList = nbt.getList("entities", NbtElement.COMPOUND_TYPE);

        for (NbtElement entityElement : entitiesList) {
            NbtCompound entityCompound = (NbtCompound) entityElement;
            UUID entityId = entityCompound.getUuid("entityId");
            boolean isPersistent = entityCompound.getBoolean("isPersistent");

            NbtList locationsList = entityCompound.getList("locations", NbtElement.COMPOUND_TYPE);
            Map<String, SavedLocation> locations = new HashMap<>();

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

                locations.put(id, new SavedLocation(position, dimension, yaw, pitch));
            }

            state.entityLocations.put(entityId, new EntityLocationData(locations, isPersistent));
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList entitiesList = new NbtList();

        for (Map.Entry<UUID, EntityLocationData> entityEntry : entityLocations.entrySet()) {
            NbtCompound entityCompound = new NbtCompound();
            entityCompound.putUuid("entityId", entityEntry.getKey());
            entityCompound.putBoolean("isPersistent", entityEntry.getValue().isPersistent);

            NbtList locationsList = new NbtList();
            for (Map.Entry<String, SavedLocation> locationEntry : entityEntry.getValue().locations.entrySet()) {
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

    // Inner class to store location data with persistence flag
    private static class EntityLocationData {
        private final Map<String, SavedLocation> locations;
        private boolean isPersistent;

        public EntityLocationData(boolean isPersistent) {
            this.locations = new HashMap<>();
            this.isPersistent = isPersistent;
        }

        public EntityLocationData(Map<String, SavedLocation> locations, boolean isPersistent) {
            this.locations = locations;
            this.isPersistent = isPersistent;
        }
    }

    public record SavedLocation(Vec3d position, RegistryKey<World> dimension, float yaw, float pitch) {}
}