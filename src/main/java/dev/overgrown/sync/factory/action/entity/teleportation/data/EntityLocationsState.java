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

/**
 * Server-global persistent storage for entity-saved teleport locations.
 *
 * <p>Anchored on the overworld's {@code PersistentStateManager} (same convention vanilla
 * uses for {@code MapState}) so that locations saved in one dimension are visible from
 * any other dimension. This is critical because {@code save_location} and
 * {@code teleport_to_location} can run in different worlds.</p>
 *
 * <p>Locations may be either {@link Static} (frozen position) or {@link Tracked}
 * (follows an entity by UUID; resolves to that entity's current position at teleport
 * time). Tracked locations carry a position snapshot used as a fallback if the entity
 * has been unloaded or removed.</p>
 */
public class EntityLocationsState extends PersistentState {
    private static final String STATE_NAME = "sync_entity_locations";

    private final Map<UUID, EntityLocationData> entityLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastAccessTime = new ConcurrentHashMap<>();

    private static final long CLEANUP_INTERVAL = 20 * 60 * 10; // 10 minutes
    private static final long MAX_INACTIVE_TIME = 20 * 60 * 30; // 30 minutes for non-persistent entities
    private long lastCleanupTime = 0;

    public EntityLocationsState() {
        super();
    }

    /**
     * Returns the server-global state, anchored to the overworld.
     * Preferred over {@link #get(ServerWorld)} for cross-dimension consistency.
     */
    public static EntityLocationsState get(MinecraftServer server) {
        return get(server.getOverworld());
    }

    /**
     * Returns the server-global state. Despite the {@code ServerWorld} parameter,
     * the state is always loaded from the overworld so saves are visible across dimensions.
     */
    public static EntityLocationsState get(ServerWorld world) {
        ServerWorld anchor = world.getServer().getOverworld();
        return anchor.getPersistentStateManager().getOrCreate(
                EntityLocationsState::fromNbt,
                EntityLocationsState::new,
                STATE_NAME
        );
    }

    public void saveStaticLocation(UUID entityId, String id, Vec3d position, RegistryKey<World> dimension,
                                   float yaw, float pitch, boolean overwrite, boolean isPersistent) {
        putLocation(entityId, id, new Static(position, dimension, yaw, pitch), overwrite, isPersistent);
    }

    public void saveTrackedLocation(UUID entityId, String id, UUID targetUuid, Vec3d snapshotPos,
                                    RegistryKey<World> snapshotDim, float snapshotYaw, float snapshotPitch,
                                    boolean overwrite, boolean isPersistent) {
        putLocation(entityId, id, new Tracked(targetUuid, snapshotPos, snapshotDim, snapshotYaw, snapshotPitch),
                overwrite, isPersistent);
    }

    private void putLocation(UUID entityId, String id, SavedLocation location, boolean overwrite, boolean isPersistent) {
        EntityLocationData entityData = entityLocations.computeIfAbsent(entityId,
                k -> new EntityLocationData(isPersistent));

        if (!overwrite && entityData.locations.containsKey(id)) {
            return;
        }

        entityData.locations.put(id, location);
        if (isPersistent) {
            entityData.isPersistent = true;
        }
        lastAccessTime.put(entityId, System.currentTimeMillis());
        markDirty();
    }

    @Nullable
    public SavedLocation getLocation(UUID entityId, String id) {
        EntityLocationData entityData = entityLocations.get(entityId);
        if (entityData == null) {
            return null;
        }
        lastAccessTime.put(entityId, System.currentTimeMillis());
        return entityData.locations.get(id);
    }

    /**
     * Resolves a saved location into a concrete world position. For {@link Tracked}
     * locations, this scans the server's worlds for the target entity and returns its
     * live position; if the entity is gone, falls back to the last-known snapshot.
     */
    @Nullable
    public ResolvedLocation resolve(MinecraftServer server, UUID entityId, String id) {
        SavedLocation saved = getLocation(entityId, id);
        if (saved == null) {
            return null;
        }
        return resolveSaved(server, saved);
    }

    public static ResolvedLocation resolveSaved(MinecraftServer server, SavedLocation saved) {
        if (saved instanceof Static s) {
            return new ResolvedLocation(s.position(), s.dimension(), s.yaw(), s.pitch());
        }
        if (saved instanceof Tracked t) {
            for (ServerWorld world : server.getWorlds()) {
                Entity entity = world.getEntity(t.targetUuid());
                if (entity != null && entity.isAlive()) {
                    return new ResolvedLocation(
                            entity.getPos(),
                            entity.getWorld().getRegistryKey(),
                            entity.getYaw(),
                            entity.getPitch()
                    );
                }
            }
            // Fallback to snapshot when the tracked entity is unavailable.
            return new ResolvedLocation(t.snapshotPosition(), t.snapshotDimension(), t.snapshotYaw(), t.snapshotPitch());
        }
        return null;
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

    public void cleanupLocations(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime < CLEANUP_INTERVAL * 50) {
            return;
        }

        lastCleanupTime = currentTime;
        boolean changed = false;

        Iterator<Map.Entry<UUID, EntityLocationData>> iterator = entityLocations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, EntityLocationData> entry = iterator.next();
            UUID entityId = entry.getKey();
            EntityLocationData data = entry.getValue();

            if (isPlayer(entityId, server)) {
                continue;
            }

            if (!data.isPersistent) {
                boolean entityExists = false;

                for (ServerWorld world : server.getWorlds()) {
                    Entity entity = world.getEntity(entityId);
                    if (entity != null && entity.isAlive()) {
                        entityExists = true;
                        if (entity.hasCustomName() || entity instanceof PathAwareEntity) {
                            data.isPersistent = true;
                        }
                        break;
                    }
                }

                if (!entityExists) {
                    Long lastAccess = lastAccessTime.get(entityId);
                    if (lastAccess == null
                            || currentTime - lastAccess > MAX_INACTIVE_TIME * 50) {
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
                    dimension = RegistryKey.of(RegistryKeys.WORLD,
                            new Identifier(locationCompound.getString("dimension")));
                }
                float yaw = locationCompound.getFloat("yaw");
                float pitch = locationCompound.getFloat("pitch");

                SavedLocation saved;
                if (locationCompound.containsUuid("target_uuid")) {
                    UUID target = locationCompound.getUuid("target_uuid");
                    saved = new Tracked(target, position, dimension, yaw, pitch);
                } else {
                    saved = new Static(position, dimension, yaw, pitch);
                }
                locations.put(id, saved);
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

                SavedLocation loc = locationEntry.getValue();
                Vec3d pos = loc.position();
                RegistryKey<World> dim = loc.dimension();
                locationCompound.putDouble("x", pos.x);
                locationCompound.putDouble("y", pos.y);
                locationCompound.putDouble("z", pos.z);
                locationCompound.putString("dimension", dim.getValue().toString());
                locationCompound.putFloat("yaw", loc.yaw());
                locationCompound.putFloat("pitch", loc.pitch());

                if (loc instanceof Tracked t) {
                    locationCompound.putUuid("target_uuid", t.targetUuid());
                }

                locationsList.add(locationCompound);
            }

            entityCompound.put("locations", locationsList);
            entitiesList.add(entityCompound);
        }

        nbt.put("entities", entitiesList);
        return nbt;
    }

    private static class EntityLocationData {
        private final Map<String, SavedLocation> locations;
        private boolean isPersistent;

        EntityLocationData(boolean isPersistent) {
            this.locations = new HashMap<>();
            this.isPersistent = isPersistent;
        }

        EntityLocationData(Map<String, SavedLocation> locations, boolean isPersistent) {
            this.locations = locations;
            this.isPersistent = isPersistent;
        }
    }

    public sealed interface SavedLocation permits Static, Tracked {
        Vec3d position();
        RegistryKey<World> dimension();
        float yaw();
        float pitch();
    }

    public record Static(Vec3d position, RegistryKey<World> dimension, float yaw, float pitch)
            implements SavedLocation {}

    public record Tracked(UUID targetUuid,
                          Vec3d snapshotPosition, RegistryKey<World> snapshotDimension,
                          float snapshotYaw, float snapshotPitch) implements SavedLocation {
        @Override
        public Vec3d position() {
            return snapshotPosition;
        }

        @Override
        public RegistryKey<World> dimension() {
            return snapshotDimension;
        }

        @Override
        public float yaw() {
            return snapshotYaw;
        }

        @Override
        public float pitch() {
            return snapshotPitch;
        }
    }

    public record ResolvedLocation(Vec3d position, RegistryKey<World> dimension, float yaw, float pitch) {}
}