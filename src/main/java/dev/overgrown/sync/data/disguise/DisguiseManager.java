package dev.overgrown.sync.data.disguise;

import dev.overgrown.sync.data.disguise.payload.s2c.DisguiseUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side registry of active disguises, keyed by the actor's UUID.
 */
public class DisguiseManager {

    private static final Map<UUID, DisguiseData> DISGUISES = new ConcurrentHashMap<>();

    public static void applyDisguise(LivingEntity actor, Entity target, boolean overwrite) {
        if (actor.getWorld().isClient()) return;
        if (!overwrite && DISGUISES.containsKey(actor.getUuid())) return;

        UUID playerUuid = (target instanceof PlayerEntity) ? target.getUuid() : null;

        NbtCompound nbt = new NbtCompound();
        target.writeNbt(nbt);

        if (target instanceof PlayerEntity) {
            nbt.putString("sync$player_name", target.getName().getString());
        }

        DisguiseData data = new DisguiseData(
            Registries.ENTITY_TYPE.getId(target.getType()),
            target.getId(),
            playerUuid,
            nbt
        );

        DISGUISES.put(actor.getUuid(), data);
        broadcast(actor, new DisguiseUpdatePayload(actor.getId(), Optional.of(data)));
    }

    public static void forceApplyDisguise(LivingEntity actor, DisguiseData data) {
        if (actor.getWorld().isClient()) return;
        DISGUISES.put(actor.getUuid(), data);
        broadcast(actor, new DisguiseUpdatePayload(actor.getId(), Optional.of(data)));
    }

    public static void removeDisguise(LivingEntity actor) {
        if (actor.getWorld().isClient()) return;
        if (!DISGUISES.containsKey(actor.getUuid())) return;
        DISGUISES.remove(actor.getUuid());
        broadcast(actor, new DisguiseUpdatePayload(actor.getId(), Optional.empty()));
    }

    public static boolean isDisguised(UUID uuid) {
        return DISGUISES.containsKey(uuid);
    }

    @Nullable
    public static DisguiseData getDisguise(UUID uuid) {
        return DISGUISES.get(uuid);
    }

    public static boolean isDisguisedAs(UUID actorUuid, Entity target) {
        DisguiseData data = DISGUISES.get(actorUuid);
        if (data == null) return false;

        if (target instanceof PlayerEntity && data.getTargetPlayerUuid() != null) {
            return data.getTargetPlayerUuid().equals(target.getUuid());
        }
        return data.getTargetEntityTypeId()
            .equals(Registries.ENTITY_TYPE.getId(target.getType()));
    }

    public static void removePlayer(UUID uuid) {
        DISGUISES.remove(uuid);
    }

    public static Map<UUID, DisguiseData> getAllDisguises() {
        return Collections.unmodifiableMap(DISGUISES);
    }

    private static void broadcast(LivingEntity actor, DisguiseUpdatePayload payload) {
        if (!(actor.getWorld() instanceof ServerWorld)) return;

        for (ServerPlayerEntity player : PlayerLookup.tracking(actor)) {
            ServerPlayNetworking.send(player, payload);
        }
        if (actor instanceof ServerPlayerEntity self) {
            ServerPlayNetworking.send(self, payload);
        }
    }
}
