package dev.overgrown.sync.factory.action.entity.grant_all_powers;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.integration.PowerClearCallback;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A static registry that maps every power <em>source</em> identifier to the
 * set of power-type identifiers that have ever been successfully granted
 * under that source.
 *
 * <p>The registry is populated entirely by
 * {@link dev.overgrown.sync.mixin.grant_all_powers.PowerHolderComponentImplMixin}, which injects
 * into the {@code RETURN} point of
 * {@code PowerHolderComponentImpl#addPower(PowerType, Identifier)} and records
 * the (source → powerId) pair whenever the method returns {@code true}.
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *   <li>Cleared automatically via {@link PowerClearCallback} (triggered at the
 *       start of every data-pack reload).  After a reload the registry is
 *       repopulated naturally as Origins / Apoli re-grants powers to online
 *       players.</li>
 *   <li>{@link #clear()} can be called manually if needed (e.g. in tests).</li>
 * </ul>
 */
public final class SourcePowerRegistry {

    private SourcePowerRegistry() {}

    // source → set of power-type IDs granted under that source
    private static final ConcurrentHashMap<Identifier, Set<Identifier>> SOURCE_MAP =
            new ConcurrentHashMap<>();

    // ── Registration (called from Mixin) ─────────────────────────────────────

    /**
     * Records that {@code powerId} was granted under {@code source}.
     * Safe to call from any thread; uses a thread-safe {@link Set} per source.
     */
    public static void track(Identifier source, Identifier powerId) {
        SOURCE_MAP.computeIfAbsent(source,
                        k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(powerId);
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    /**
     * Returns an unmodifiable view of all power-type IDs that have been
     * tracked for the given {@code source}.  Returns an empty set when the
     * source is unknown (no powers granted under it yet).
     */
    public static Set<Identifier> getPowersForSource(Identifier source) {
        Set<Identifier> set = SOURCE_MAP.get(source);
        return set == null ? Set.of() : Collections.unmodifiableSet(set);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Wipes the entire registry.  Called on every data-pack reload. */
    public static void clear() {
        SOURCE_MAP.clear();
        Sync.LOGGER.debug("[Sync/SourcePowerRegistry] Registry cleared.");
    }

    /**
     * Registers the {@link PowerClearCallback} hook so the registry is reset
     * automatically on every data-pack reload.  Call this once from
     * {@code Sync#onInitialize()}.
     */
    public static void registerClearHook() {
        PowerClearCallback.EVENT.register(SourcePowerRegistry::clear);
    }
}