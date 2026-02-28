package dev.overgrown.sync.factory.data.disguise;

import dev.overgrown.sync.networking.ModPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side registry of active disguises, keyed by the actor's UUID.
 */
public class DisguiseManager {

    private static final Map<UUID, DisguiseData> DISGUISES = new ConcurrentHashMap<>();

    // Public API
    /**
     * Apply a disguise to {@code actor}, making them appear as {@code target}.
     *
     * @param overwrite when {@code false} the disguise is skipped if the actor
     *                  is already disguised as something else.
     */
    public static void applyDisguise(LivingEntity actor, Entity target, boolean overwrite) {
        if (actor.getWorld().isClient()) return; // ignore client-side calls

        if (!overwrite && DISGUISES.containsKey(actor.getUuid())) {
            return;
        }

        UUID playerUuid = (target instanceof PlayerEntity) ? target.getUuid() : null;
        Text displayName = target.getCustomName() != null
                ? target.getCustomName()
                : target.getName();

        DisguiseData data = new DisguiseData(
                Registries.ENTITY_TYPE.getId(target.getType()),
                target.getId(),
                playerUuid,
                displayName
        );

        DISGUISES.put(actor.getUuid(), data);
        syncSet(actor, data);
    }

    public static void forceApplyDisguise(LivingEntity actor, DisguiseData data) {
        if (actor.getWorld().isClient()) return;
        DISGUISES.put(actor.getUuid(), data);
        syncSet(actor, data);
    }

    /** Remove the actor's current disguise, if any. */
    public static void removeDisguise(LivingEntity actor) {
        if (actor.getWorld().isClient()) return; // ignore client-side calls

        if (!DISGUISES.containsKey(actor.getUuid())) return;
        DISGUISES.remove(actor.getUuid());
        syncRemove(actor);
    }

    public static boolean isDisguised(UUID uuid) {
        return DISGUISES.containsKey(uuid);
    }

    @Nullable
    public static DisguiseData getDisguise(UUID uuid) {
        return DISGUISES.get(uuid);
    }

    /**
     * Returns {@code true} when the actor is disguised as {@code target}.
     * <ul>
     *   <li>If target is a player: checks UUID equality.</li>
     *   <li>Otherwise: checks entity type equality.</li>
     * </ul>
     */
    public static boolean isDisguisedAs(UUID actorUuid, Entity target) {
        DisguiseData data = DISGUISES.get(actorUuid);
        if (data == null) return false;

        if (target instanceof PlayerEntity && data.getTargetPlayerUuid() != null) {
            return data.getTargetPlayerUuid().equals(target.getUuid());
        }
        return data.getTargetEntityTypeId()
                .equals(Registries.ENTITY_TYPE.getId(target.getType()));
    }

    /** Remove all disguise state when a player leaves. */
    public static void removePlayer(UUID uuid) {
        DISGUISES.remove(uuid);
    }

    /** Read-only view used for syncing to newly joined players. */
    public static Map<UUID, DisguiseData> getAllDisguises() {
        return Collections.unmodifiableMap(DISGUISES);
    }

    // Networking
    private static void syncSet(LivingEntity actor, DisguiseData data) {
        PacketByteBuf buf = buildSetPacket(actor.getId(), data);
        broadcast(actor, buf);
    }

    private static void syncRemove(LivingEntity actor) {
        PacketByteBuf buf = buildRemovePacket(actor.getId());
        broadcast(actor, buf);
    }

    public static PacketByteBuf buildSetPacket(int entityNetId, DisguiseData data) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(entityNetId);
        buf.writeBoolean(true);
        data.write(buf);
        return buf;
    }

    public static PacketByteBuf buildRemovePacket(int entityNetId) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(entityNetId);
        buf.writeBoolean(false);
        return buf;
    }

    private static void broadcast(LivingEntity actor, PacketByteBuf buf) {
        if (!(actor.getWorld() instanceof ServerWorld)) return;

        for (ServerPlayerEntity player : PlayerLookup.tracking(actor)) {
            ServerPlayNetworking.send(player, ModPackets.DISGUISE_UPDATE, new PacketByteBuf(buf.copy()));
        }
        if (actor instanceof ServerPlayerEntity self) {
            ServerPlayNetworking.send(self, ModPackets.DISGUISE_UPDATE, new PacketByteBuf(buf.copy()));
        }
    }
}