package dev.overgrown.sync.action.type.entity.grant_all_powers;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.integration.PowerClearCallback;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry mapping power source identifiers to the set of power-type identifiers
 * that have ever been granted under that source.
 */
public final class SourcePowerRegistry {

    private SourcePowerRegistry() {}

    private static final ConcurrentHashMap<Identifier, Set<Identifier>> SOURCE_MAP = new ConcurrentHashMap<>();

    public static void track(Identifier source, Identifier powerId) {
        SOURCE_MAP.computeIfAbsent(source,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
            .add(powerId);
    }

    public static Set<Identifier> getPowersForSource(Identifier source) {
        Set<Identifier> set = SOURCE_MAP.get(source);
        return set == null ? Set.of() : Collections.unmodifiableSet(set);
    }

    public static void clear() {
        SOURCE_MAP.clear();
        Sync.LOGGER.debug("[Sync/SourcePowerRegistry] Registry cleared.");
    }

    public static void registerClearHook() {
        PowerClearCallback.EVENT.register(SourcePowerRegistry::clear);
    }
}
