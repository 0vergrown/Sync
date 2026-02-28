package dev.overgrown.sync.factory.data.disguise.client;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.mixin.disguise.accessor.LimbAnimatorAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side counterpart to {@link dev.overgrown.sync.factory.data.disguise.DisguiseManager}.
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

    /**
     * Returns {@code true} if the actor (with network ID actorNetId) is disguised
     * as the given target entity.
     */
    public static boolean isDisguisedAs(int actorNetId, Entity target) {
        DisguiseData data = DISGUISES.get(actorNetId);
        if (data == null) return false;

        if (target instanceof PlayerEntity && data.getTargetPlayerUuid() != null) {
            return data.getTargetPlayerUuid().equals(target.getUuid());
        }
        return data.getTargetEntityTypeId()
                .equals(Registries.ENTITY_TYPE.getId(target.getType()));
    }

    /** Returns {@code true} if {@code entityNetId} is disguised specifically as player {@code uuid}. */
    public static boolean isDisguisedAsPlayer(int entityNetId, UUID uuid) {
        DisguiseData data = DISGUISES.get(entityNetId);
        return data != null && uuid.equals(data.getTargetPlayerUuid());
    }

    @Nullable
    public static Entity getSyncedDummy(int entityNetId, Entity actor) {
        Entity dummy = DUMMY_ENTITIES.get(entityNetId);
        if (dummy == null) return null;

        dummy.setPos(actor.getX(), actor.getY(), actor.getZ());
        dummy.prevX = actor.prevX;
        dummy.prevY = actor.prevY;
        dummy.prevZ = actor.prevZ;
        dummy.lastRenderX = actor.lastRenderX;
        dummy.lastRenderY = actor.lastRenderY;
        dummy.lastRenderZ = actor.lastRenderZ;

        dummy.setYaw(actor.getYaw());
        dummy.setPitch(actor.getPitch());
        dummy.prevYaw = actor.prevYaw;
        dummy.prevPitch = actor.prevPitch;
        dummy.age = actor.age;

        if (dummy instanceof LivingEntity dLiving && actor instanceof LivingEntity aLiving) {
            dLiving.bodyYaw     = aLiving.bodyYaw;
            dLiving.prevBodyYaw = aLiving.prevBodyYaw;
            dLiving.headYaw     = aLiving.headYaw;
            dLiving.prevHeadYaw = aLiving.prevHeadYaw;
            dLiving.hurtTime    = aLiving.hurtTime;
            dLiving.deathTime   = aLiving.deathTime;

            dLiving.handSwingProgress     = aLiving.handSwingProgress;
            dLiving.lastHandSwingProgress = aLiving.lastHandSwingProgress;

            LimbAnimatorAccessor dummyLimb = (LimbAnimatorAccessor) (Object) dLiving.limbAnimator;
            LimbAnimatorAccessor realLimb  = (LimbAnimatorAccessor) (Object) aLiving.limbAnimator;
            dLiving.limbAnimator.setSpeed(aLiving.limbAnimator.getSpeed());
            dummyLimb.sync$setPrevSpeed(realLimb.sync$getPrevSpeed());
            dummyLimb.sync$setPos(realLimb.sync$getPos());
        }

        return dummy;
    }

    private static void createDummy(int entityNetId, DisguiseData data) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        var entityType = Registries.ENTITY_TYPE.get(data.getTargetEntityTypeId());
        if (entityType == null) return;

        try {
            Entity dummy = entityType.create(client.world);
            if (dummy != null) {
                dummy.setId(-entityNetId - 1);
                DUMMY_ENTITIES.put(entityNetId, dummy);
            } else {
                Sync.LOGGER.warn("[Sync] Disguise dummy creation returned null for type: {}", data.getTargetEntityTypeId());
            }
        } catch (Exception e) {
            Sync.LOGGER.warn("[Sync] Could not create dummy entity for disguise (type={}): {}",
                    data.getTargetEntityTypeId(), e.getMessage());
        }
    }
}