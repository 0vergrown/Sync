package dev.overgrown.sync.factory.power.type.entity_set;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntitySetPower extends Power {

    private final Consumer<Pair<Entity, Entity>> actionOnAdd;
    private final Consumer<Pair<Entity, Entity>> actionOnRemove;
    private final int tickRate;

    private final Set<UUID> entityUuids = new HashSet<>();
    private final Map<UUID, Entity> entities = new HashMap<>();

    private final Set<UUID> tempUuids = new HashSet<>();
    private final Map<UUID, Long> tempEntities = new ConcurrentHashMap<>();

    private Integer startTicks = null;

    private boolean wasActive = false;
    private boolean removedTemps = false;

    public EntitySetPower(PowerType<?> type, LivingEntity entity,
                          Consumer<Pair<Entity, Entity>> actionOnAdd,
                          Consumer<Pair<Entity, Entity>> actionOnRemove,
                          int tickRate) {
        super(type, entity);
        if (tickRate <= 0) {
            throw new IllegalArgumentException("Tick rate must be a positive integer");
        }
        this.actionOnAdd = actionOnAdd;
        this.actionOnRemove = actionOnRemove;
        this.tickRate = tickRate;
        this.setTicking(true);
    }

    @Override
    public void onAdded() {
        removedTemps = entityUuids.removeIf(tempUuids::contains);
        tempUuids.clear();
    }

    @Override
    public void tick() {
        if (removedTemps) {
            this.removedTemps = false;
            PowerHolderComponent.syncPower(this.entity, this.type);
            return;
        }

        if (!tempEntities.isEmpty() && this.isActive()) {
            if (startTicks == null) {
                this.startTicks = entity.age % tickRate;
                return;
            }
            if (entity.age % tickRate == startTicks) {
                this.tickTempEntities();
            }
            this.wasActive = true;
        } else if (wasActive) {
            this.startTicks = null;
            this.wasActive = false;
        }
    }

    protected void tickTempEntities() {
        Iterator<Map.Entry<UUID, Long>> entryIterator = tempEntities.entrySet().iterator();
        long time = entity.getWorld().getTime();

        while (entryIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = entryIterator.next();
            if (time < entry.getValue()) continue;

            UUID uuid = entry.getKey();
            Entity tempEntity = this.getEntity(uuid);

            entryIterator.remove();
            if (entityUuids.remove(uuid) | entities.remove(uuid) != null | tempUuids.remove(uuid)) {
                if (actionOnRemove != null) {
                    actionOnRemove.accept(new Pair<>(entity, tempEntity));
                }
                this.removedTemps = true;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------------------------

    /**
     * Eagerly evicts any entity that is cached locally but has already been
     * destroyed (killed / discarded) since the last server tick.  This is a
     * cheap O(n) pass over the in-memory cache; it does <em>not</em> perform
     * world lookups.  Call {@link #validateEntities()} when you also want to
     * purge UUIDs that were never loaded into the cache.
     *
     * @return {@code true} if at least one entry was removed.
     */
    private boolean evictDestroyed() {
        boolean changed = false;
        Iterator<UUID> iter = entityUuids.iterator();
        while (iter.hasNext()) {
            UUID uuid = iter.next();
            Entity cached = entities.get(uuid);
            // Only evict if we have a cached reference that is definitively gone.
            if (cached != null && cached.isRemoved()
                    && cached.getRemovalReason() != null
                    && cached.getRemovalReason().shouldDestroy()) {
                iter.remove();
                entities.remove(uuid);
                tempUuids.remove(uuid);
                tempEntities.remove(uuid);
                if (actionOnRemove != null) {
                    actionOnRemove.accept(new Pair<>(entity, cached));
                }
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Walks every UUID in the set and removes entries whose entity can no
     * longer be found in any loaded world.  This is the authoritative cleanup
     * path; it is more expensive than {@link #evictDestroyed()} but covers
     * UUIDs whose entity was unloaded before a reference was cached here.
     *
     * @return {@code true} if every remaining member could be located.
     */
    public boolean validateEntities() {
        MinecraftServer server = entity.getServer();
        if (server == null) return false;

        Iterator<UUID> uuidIterator = entityUuids.iterator();
        boolean valid = true;

        while (uuidIterator.hasNext()) {
            UUID uuid = uuidIterator.next();
            if (getEntityFromAllWorlds(server, uuid) != null) continue;

            uuidIterator.remove();
            entities.remove(uuid);
            tempUuids.remove(uuid);
            tempEntities.remove(uuid);
            valid = false;
        }

        return valid;
    }

    @Nullable
    private static Entity getEntityFromAllWorlds(MinecraftServer server, UUID uuid) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity != null) return entity;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Mutation API
    // -------------------------------------------------------------------------

    public boolean add(Entity entity) {
        return add(entity, null);
    }

    public boolean add(Entity entity, @Nullable Integer time) {
        if (entity == null || entity.isRemoved() || entity.getWorld().isClient) {
            return false;
        }

        UUID uuid = entity.getUuid();
        boolean addedToSet = false;

        if (time != null) {
            addedToSet |= tempUuids.add(uuid);
            tempEntities.compute(uuid, (prevUuid, prevTime) ->
                    entity.getWorld().getTime() + time);
        }

        if (!entityUuids.contains(uuid)) {
            addedToSet |= entityUuids.add(uuid);
            entities.put(uuid, entity);
            if (actionOnAdd != null) {
                actionOnAdd.accept(new Pair<>(this.entity, entity));
            }
        }

        return addedToSet;
    }

    public boolean remove(@Nullable Entity entity) {
        return remove(entity, true);
    }

    public boolean remove(@Nullable Entity entity, boolean executeRemoveAction) {
        if (entity == null || entity.getWorld().isClient) return false;

        UUID uuid = entity.getUuid();
        boolean result = entityUuids.remove(uuid)
                | entities.remove(uuid) != null
                | tempUuids.remove(uuid)
                | tempEntities.remove(uuid) != null;

        if (executeRemoveAction && result && actionOnRemove != null) {
            actionOnRemove.accept(new Pair<>(this.entity, entity));
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // Query API
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code entity} is a member of this set <em>and</em>
     * is not already marked as destroyed.  This prevents stale entries from
     * evaluating as present in the brief window between entity death and the
     * next cleanup sweep.
     */
    public boolean contains(Entity entity) {
        if (entity == null) return false;

        UUID uuid = entity.getUuid();
        if (!entityUuids.contains(uuid)) return false;

        // If the entity is removed/killed, evict it immediately rather than
        // reporting a false positive.
        if (entity.isRemoved()
                && entity.getRemovalReason() != null
                && entity.getRemovalReason().shouldDestroy()) {
            entityUuids.remove(uuid);
            entities.remove(uuid);
            tempUuids.remove(uuid);
            tempEntities.remove(uuid);
            if (actionOnRemove != null) {
                actionOnRemove.accept(new Pair<>(this.entity, entity));
            }
            return false;
        }

        return true;
    }

    /**
     * Returns the number of entities currently in the set, automatically
     * evicting any cached entries that have already been destroyed.  This
     * makes the count accurate in the same tick that an entity dies, without
     * requiring a full world-scan.
     */
    public int size() {
        if (evictDestroyed()) {
            PowerHolderComponent.syncPower(this.entity, this.type);
        }
        return entityUuids.size();
    }

    public void clear() {
        if (actionOnRemove != null) {
            for (UUID entityUuid : entityUuids) {
                actionOnRemove.accept(new Pair<>(this.entity, this.getEntity(entityUuid)));
            }
        }

        boolean wasNotEmpty = !entityUuids.isEmpty() || !tempUuids.isEmpty();
        tempUuids.clear();
        tempEntities.clear();
        entityUuids.clear();
        entities.clear();

        if (wasNotEmpty) {
            PowerHolderComponent.syncPower(this.entity, this.type);
        }
    }

    /**
     * Returns a snapshot of the UUID set for safe iteration.  Dead entries may
     * still be present; callers should guard against {@code null} from
     * {@link #getEntity(UUID)}.
     */
    public Set<UUID> getIterationSet() {
        return new HashSet<>(entityUuids);
    }

    @Nullable
    public Entity getEntity(UUID uuid) {
        if (!entityUuids.contains(uuid)) return null;

        Entity entity = entities.get(uuid);

        // If the cached reference has been destroyed, evict and return null.
        if (entity != null) {
            if (entity.isRemoved()
                    && entity.getRemovalReason() != null
                    && entity.getRemovalReason().shouldDestroy()) {
                entityUuids.remove(uuid);
                entities.remove(uuid);
                tempUuids.remove(uuid);
                tempEntities.remove(uuid);
                if (actionOnRemove != null) {
                    actionOnRemove.accept(new Pair<>(this.entity, entity));
                }
                PowerHolderComponent.syncPower(this.entity, this.type);
                return null;
            }
            if (!entity.isRemoved()) return entity;
        }

        // Cache miss or soft-removed entity: look it up across all worlds.
        MinecraftServer server = this.entity.getServer();
        if (server != null) {
            entity = getEntityFromAllWorlds(server, uuid);
            if (entity != null) {
                entities.put(uuid, entity);
                return entity;
            }
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // NBT serialisation
    // -------------------------------------------------------------------------

    @Override
    public NbtElement toTag() {
        NbtCompound rootNbt = new NbtCompound();

        NbtList entityUuidsNbt = new NbtList();
        NbtList tempUuidsNbt = new NbtList();

        for (UUID entityUuid : entityUuids) {
            entityUuidsNbt.add(NbtHelper.fromUuid(entityUuid));
        }
        for (UUID tempUuid : tempUuids) {
            tempUuidsNbt.add(NbtHelper.fromUuid(tempUuid));
        }

        rootNbt.put("Entities", entityUuidsNbt);
        rootNbt.put("TempEntities", tempUuidsNbt);
        rootNbt.putBoolean("RemovedTemps", removedTemps);

        return rootNbt;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if (!(tag instanceof NbtCompound rootNbt)) return;

        tempUuids.clear();
        tempEntities.clear();
        entityUuids.clear();
        entities.clear();

        NbtList tempUuidsNbt = rootNbt.getList("TempEntities", NbtElement.INT_ARRAY_TYPE);
        for (NbtElement tempUuidNbt : tempUuidsNbt) {
            tempUuids.add(NbtHelper.toUuid(tempUuidNbt));
        }

        NbtList entityUuidsNbt = rootNbt.getList("Entities", NbtElement.INT_ARRAY_TYPE);
        for (NbtElement entityUuidNbt : entityUuidsNbt) {
            entityUuids.add(NbtHelper.toUuid(entityUuidNbt));
        }

        removedTemps = rootNbt.getBoolean("RemovedTemps");
    }

    // -------------------------------------------------------------------------
    // Static Fabric event integration callbacks – register these in your mod
    // initializer via ServerEntityEvents.ENTITY_LOAD / ENTITY_UNLOAD.
    // -------------------------------------------------------------------------

    public static void integrateLoadCallback(Entity loadedEntity, ServerWorld world) {
        PowerHolderComponent.KEY.maybeGet(loadedEntity).ifPresent(component ->
                component.getPowers(EntitySetPower.class, true).stream()
                        .filter(Predicate.not(EntitySetPower::validateEntities))
                        .map(Power::getType)
                        .forEach(powerType -> PowerHolderComponent.syncPower(loadedEntity, powerType)));
    }

    /**
     * Removes a permanently-destroyed entity from every set that contains it.
     * Player deaths are excluded here because a dead player respawns rather
     * than ceasing to exist; handle player-specific cleanup (e.g. disconnect)
     * separately.
     */
    public static void integrateUnloadCallback(Entity unloadedEntity, ServerWorld world) {
        Entity.RemovalReason removalReason = unloadedEntity.getRemovalReason();
        if (removalReason == null || !removalReason.shouldDestroy()
                || unloadedEntity instanceof PlayerEntity) {
            return;
        }

        for (ServerWorld otherWorld : world.getServer().getWorlds()) {
            for (Entity entity : otherWorld.iterateEntities()) {
                PowerHolderComponent.KEY.maybeGet(entity).ifPresent(component ->
                        component.getPowers(EntitySetPower.class, true).stream()
                                .filter(p -> p.remove(unloadedEntity, false))
                                .map(Power::getType)
                                .forEach(powerType ->
                                        PowerHolderComponent.syncPower(entity, powerType)));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    public static PowerFactory<EntitySetPower> getFactory() {
        return new PowerFactory<EntitySetPower>(
                Sync.identifier("entity_set"),
                new SerializableData()
                        .add("action_on_add", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("action_on_remove", ApoliDataTypes.BIENTITY_ACTION, null)
                        .add("tick_rate", SerializableDataTypes.INT, 1),
                data -> (powerType, livingEntity) -> new EntitySetPower(
                        powerType,
                        livingEntity,
                        data.get("action_on_add"),
                        data.get("action_on_remove"),
                        data.getInt("tick_rate")
                )
        ).allowCondition();
    }
}