package dev.overgrown.sync.data.teleportation;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-world persistent storage of named entity locations.
 *
 * <p>Re-implementation of Sync's 1.20.1 EntityLocationsState using 1.21's
 * Codec-aware {@link PersistentState.Type} API. Behaves identically from a
 * gameplay perspective: each entity (keyed by UUID) can store any number of
 * named locations, retrievable later by id.</p>
 */
public class EntityLocationsState extends PersistentState {

    public static final String STATE_NAME = "sync_entity_locations";

    private final Map<UUID, Map<String, SavedLocation>> entityLocations = new ConcurrentHashMap<>();

    public EntityLocationsState() {
    }

    public static PersistentState.Type<EntityLocationsState> getType() {
        return new PersistentState.Type<>(
            EntityLocationsState::new,
            EntityLocationsState::fromNbt,
            DataFixTypes.LEVEL
        );
    }

    public static EntityLocationsState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(getType(), STATE_NAME);
    }

    public void saveLocation(UUID entityId, String id, Vec3d position, RegistryKey<World> dimension,
                             float yaw, float pitch, boolean overwrite) {
        Map<String, SavedLocation> locations = entityLocations.computeIfAbsent(entityId, k -> new HashMap<>());
        if (!overwrite && locations.containsKey(id)) {
            return;
        }
        locations.put(id, new SavedLocation(position, dimension, yaw, pitch));
        markDirty();
    }

    @Nullable
    public SavedLocation getLocation(UUID entityId, String id) {
        Map<String, SavedLocation> locations = entityLocations.get(entityId);
        return locations == null ? null : locations.get(id);
    }

    public boolean removeLocation(UUID entityId, String id) {
        Map<String, SavedLocation> locations = entityLocations.get(entityId);
        if (locations == null) return false;
        boolean removed = locations.remove(id) != null;
        if (removed) markDirty();
        return removed;
    }

    public void removeAllLocationsForEntity(UUID entityId) {
        if (entityLocations.remove(entityId) != null) {
            markDirty();
        }
    }

    private static final long CLEANUP_INTERVAL = 20L * 60L * 10L; // 10 minutes worth of ticks
    private long lastCleanupTime = 0;

    /**
     * Periodically prunes locations whose entity UUID can no longer be found
     * in any loaded world (and is not a player or named/path-aware entity).
     */
    public void cleanupLocations(MinecraftServer server) {
        long now = server.getOverworld().getTime();
        if (now - lastCleanupTime < CLEANUP_INTERVAL) return;
        lastCleanupTime = now;

        Set<UUID> liveEntities = new HashSet<>();
        for (ServerWorld world : server.getWorlds()) {
            world.iterateEntities().forEach(entity -> liveEntities.add(entity.getUuid()));
        }

        Iterator<UUID> iter = entityLocations.keySet().iterator();
        boolean changed = false;
        while (iter.hasNext()) {
            UUID id = iter.next();
            if (liveEntities.contains(id)) continue;

            // Try to find an offline entity with this UUID before purging.
            Entity found = null;
            for (ServerWorld world : server.getWorlds()) {
                Entity e = world.getEntity(id);
                if (e != null) {
                    found = e;
                    break;
                }
            }
            if (found != null && shouldKeepData(found)) continue;

            iter.remove();
            changed = true;
        }

        if (changed) markDirty();
    }

    private static boolean shouldKeepData(Entity entity) {
        return entity instanceof PlayerEntity
            || entity.hasCustomName()
            || entity instanceof PathAwareEntity;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList entitiesList = new NbtList();
        for (Map.Entry<UUID, Map<String, SavedLocation>> entityEntry : entityLocations.entrySet()) {
            NbtCompound entityNbt = new NbtCompound();
            entityNbt.putUuid("entityId", entityEntry.getKey());

            NbtList locationsList = new NbtList();
            for (Map.Entry<String, SavedLocation> locEntry : entityEntry.getValue().entrySet()) {
                NbtCompound locNbt = new NbtCompound();
                SavedLocation loc = locEntry.getValue();
                locNbt.putString("id", locEntry.getKey());
                locNbt.putDouble("x", loc.position().x);
                locNbt.putDouble("y", loc.position().y);
                locNbt.putDouble("z", loc.position().z);
                locNbt.putString("dimension", loc.dimension().getValue().toString());
                locNbt.putFloat("yaw", loc.yaw());
                locNbt.putFloat("pitch", loc.pitch());
                locationsList.add(locNbt);
            }

            entityNbt.put("locations", locationsList);
            entitiesList.add(entityNbt);
        }
        nbt.put("entities", entitiesList);
        return nbt;
    }

    public static EntityLocationsState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        EntityLocationsState state = new EntityLocationsState();
        NbtList entitiesList = nbt.getList("entities", NbtElement.COMPOUND_TYPE);

        for (NbtElement entityElement : entitiesList) {
            NbtCompound entityNbt = (NbtCompound) entityElement;
            UUID entityId = entityNbt.getUuid("entityId");

            Map<String, SavedLocation> locations = new HashMap<>();
            NbtList locationsList = entityNbt.getList("locations", NbtElement.COMPOUND_TYPE);
            for (NbtElement locElement : locationsList) {
                NbtCompound locNbt = (NbtCompound) locElement;
                String id = locNbt.getString("id");
                Vec3d pos = new Vec3d(locNbt.getDouble("x"), locNbt.getDouble("y"), locNbt.getDouble("z"));
                RegistryKey<World> dimension = locNbt.contains("dimension")
                    ? RegistryKey.of(RegistryKeys.WORLD, Identifier.of(locNbt.getString("dimension")))
                    : World.OVERWORLD;
                locations.put(id, new SavedLocation(pos, dimension, locNbt.getFloat("yaw"), locNbt.getFloat("pitch")));
            }

            state.entityLocations.put(entityId, locations);
        }

        return state;
    }

    public record SavedLocation(Vec3d position, RegistryKey<World> dimension, float yaw, float pitch) {}
}
