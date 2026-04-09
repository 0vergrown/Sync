package dev.overgrown.sync.factory.condition.entity.perspective.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores each player's current camera perspective (as a lower-snake-case string)
 * on the logical server.  The client sends a {@code sync:perspective_update} packet
 * whenever the value changes; see {@code SyncClient} for the sender and
 * {@code Sync#onInitialize} for the receiver.
 *
 * <p>Valid values: {@code "first_person"}, {@code "third_person_back"},
 * {@code "third_person_front"}.
 */
public class PerspectiveManager {

    private static final Map<UUID, String> PERSPECTIVES = new ConcurrentHashMap<>();

    /** Called from the server-side packet receiver. */
    public static void setPerspective(ServerPlayerEntity player, String perspective) {
        PERSPECTIVES.put(player.getUuid(), perspective);
    }

    /**
     * Returns the last-known perspective for {@code player},
     * defaulting to {@code "first_person"} if no packet has arrived yet.
     */
    public static String getPerspective(PlayerEntity player) {
        return PERSPECTIVES.getOrDefault(player.getUuid(), "first_person");
    }

    /** Called on disconnect to prevent memory leaks. */
    public static void removePlayer(UUID uuid) {
        PERSPECTIVES.remove(uuid);
    }
}