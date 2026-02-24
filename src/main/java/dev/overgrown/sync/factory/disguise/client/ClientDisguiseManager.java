package dev.overgrown.sync.factory.disguise.client;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.disguise.DisguiseData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side counterpart to {@link dev.overgrown.sync.factory.disguise.DisguiseManager}.
 * Keyed by the entity's *network* (integer) ID.
 */
@Environment(EnvType.CLIENT)
public class ClientDisguiseManager {

    /** Disguise metadata, keyed by the actor's network entity ID. */
    private static final Map<Integer, DisguiseData> DISGUISES = new ConcurrentHashMap<>();

    /**
     * Dummy entity instances used as a rendering proxy for non-player disguises.
     * Created once per disguise and reused every frame.
     */
    private static final Map<Integer, Entity> DUMMY_ENTITIES = new ConcurrentHashMap<>();

    // Mutation
    public static void setDisguise(int entityNetId, DisguiseData data) {
        DISGUISES.put(entityNetId, data);

        // Only need a dummy entity for non-player disguises (player disguises are handled purely via the PlayerListEntry mixin).
        if (!data.isPlayerDisguise()) {
            createDummy(entityNetId, data);
        } else {
            DUMMY_ENTITIES.remove(entityNetId);
        }
    }

    public static void removeDisguise(int entityNetId) {
        DISGUISES.remove(entityNetId);
        DUMMY_ENTITIES.remove(entityNetId);
    }

    /** Called when the client disconnects / world is unloaded. */
    public static void clear() {
        DISGUISES.clear();
        DUMMY_ENTITIES.clear();
    }

    // Query
    public static boolean isDisguised(int entityNetId) {
        return DISGUISES.containsKey(entityNetId);
    }

    @Nullable
    public static DisguiseData getDisguise(int entityNetId) {
        return DISGUISES.get(entityNetId);
    }

    /** Returns {@code true} if {@code entityNetId} is disguised specifically as player {@code uuid}. */
    public static boolean isDisguisedAsPlayer(int entityNetId, UUID uuid) {
        DisguiseData data = DISGUISES.get(entityNetId);
        return data != null && uuid.equals(data.getTargetPlayerUuid());
    }

    /**
     * Returns the dummy entity for non-player disguises, after updating its
     * position / rotation to match the actor.
     */
    @Nullable
    public static Entity getSyncedDummy(int entityNetId, Entity actor) {
        Entity dummy = DUMMY_ENTITIES.get(entityNetId);
        if (dummy == null) return null;

        // Synchronize world-space state so (hopefully) animations look correct.
        dummy.setPos(actor.getX(), actor.getY(), actor.getZ());
        dummy.setYaw(actor.getYaw());
        dummy.setPitch(actor.getPitch());
        dummy.age = actor.age;

        if (dummy instanceof net.minecraft.entity.LivingEntity dLiving
                && actor instanceof net.minecraft.entity.LivingEntity aLiving) {
            dLiving.bodyYaw     = aLiving.bodyYaw;
            dLiving.prevBodyYaw = aLiving.prevBodyYaw;
            dLiving.headYaw     = aLiving.headYaw;
            dLiving.prevHeadYaw = aLiving.prevHeadYaw;
            dLiving.hurtTime    = aLiving.hurtTime;
            dLiving.deathTime   = aLiving.deathTime;
        }

        return dummy;
    }

    // Internal
    private static void createDummy(int entityNetId, DisguiseData data) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        var entityType = Registries.ENTITY_TYPE.get(data.getTargetEntityTypeId());
        if (entityType == null) return;

        try {
            Entity dummy = entityType.create(client.world);
            if (dummy != null) {
                DUMMY_ENTITIES.put(entityNetId, dummy);
            }
        } catch (Exception e) {
            Sync.LOGGER.warn("Could not create dummy entity for disguise (type={}): {}",
                    data.getTargetEntityTypeId(), e.getMessage());
        }
    }
}