package dev.overgrown.sync.data.suppress_power;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SuppressedPowerManager {

    private SuppressedPowerManager() {}

    private static final ConcurrentHashMap<UUID, Set<Identifier>> SUPPRESSED = new ConcurrentHashMap<>();

    public static void suppress(UUID entityId, Identifier powerId) {
        SUPPRESSED.computeIfAbsent(entityId,
            k -> Collections.newSetFromMap(new ConcurrentHashMap<>())
        ).add(powerId);
    }

    public static void liberate(UUID entityId, Identifier powerId) {
        Set<Identifier> set = SUPPRESSED.get(entityId);
        if (set == null) return;
        set.remove(powerId);
        if (set.isEmpty()) SUPPRESSED.remove(entityId);
    }

    public static void removeAll(UUID entityId) {
        SUPPRESSED.remove(entityId);
    }

    public static boolean isSuppressed(UUID entityId, Identifier powerId) {
        Set<Identifier> set = SUPPRESSED.get(entityId);
        return set != null && set.contains(powerId);
    }

    public static Set<Identifier> getSuppressedPowers(UUID entityId) {
        Set<Identifier> set = SUPPRESSED.get(entityId);
        return set == null ? Set.of() : Collections.unmodifiableSet(set);
    }
}
