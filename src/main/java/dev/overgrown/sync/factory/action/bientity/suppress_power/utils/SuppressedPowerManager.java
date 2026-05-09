package dev.overgrown.sync.factory.action.bientity.suppress_power.utils;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which power-type IDs are currently suppressed for a given entity UUID.
 *
 * <p>Suppression is flat (non-stacking): calling {@link #suppress} twice for the
 * same (entity, power) pair still only requires one {@link #liberate} call to
 * lift the suppression.
 *
 * <p>The map is cleared for a player on disconnect and for any entity when it
 * unloads dead.  Both cleanup hooks are registered in {@code Sync#onInitialize}.
 */
public final class SuppressedPowerManager {

    private SuppressedPowerManager() {}

    /** entity UUID → set of suppressed power-type identifiers */
    private static final ConcurrentHashMap<UUID, Set<Identifier>> SUPPRESSED =
            new ConcurrentHashMap<>();

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Marks {@code powerId} as suppressed for {@code entityId}.
     * Idempotent – calling this repeatedly for the same pair is safe.
     */
    public static void suppress(UUID entityId, Identifier powerId) {
        SUPPRESSED.computeIfAbsent(entityId,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>())
        ).add(powerId);
    }

    /**
     * Lifts the suppression of {@code powerId} for {@code entityId}.
     * Does nothing if the power was not suppressed.
     */
    public static void liberate(UUID entityId, Identifier powerId) {
        Set<Identifier> set = SUPPRESSED.get(entityId);
        if (set == null) return;
        set.remove(powerId);
        if (set.isEmpty()) SUPPRESSED.remove(entityId);
    }

    /**
     * Removes all suppressed powers for the given entity.
     * Call this on disconnect / entity unload.
     */
    public static void removeAll(UUID entityId) {
        SUPPRESSED.remove(entityId);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Returns {@code true} if {@code powerId} is currently suppressed for {@code entityId}. */
    public static boolean isSuppressed(UUID entityId, Identifier powerId) {
        Set<Identifier> set = SUPPRESSED.get(entityId);
        return set != null && set.contains(powerId);
    }

    /**
     * Returns an unmodifiable snapshot of all suppressed power IDs for {@code entityId}.
     * Returns an empty set if nothing is suppressed.
     */
    public static Set<Identifier> getSuppressedPowers(UUID entityId) {
        Set<Identifier> set = SUPPRESSED.get(entityId);
        return set == null ? Set.of() : Collections.unmodifiableSet(set);
    }
}