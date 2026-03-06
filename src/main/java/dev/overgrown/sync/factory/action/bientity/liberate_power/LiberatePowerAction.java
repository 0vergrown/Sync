package dev.overgrown.sync.factory.action.bientity.liberate_power;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bi-entity action that lifts (liberates) suppressions previously applied by
 * {@link dev.overgrown.sync.factory.action.bientity.suppress_power.SuppressPowerAction}
 * on the <em>target</em> entity.
 *
 * <p>Uses identical filter semantics to {@code suppress_power}: a power is
 * liberated if it matches any of the provided filters (or all powers if no
 * filters are given), unless it appears in {@code ignored_powers}.
 *
 * <h3>Fields</h3>
 * <table>
 *   <tr><th>Field</th><th>Type</th><th>Default</th><th>Description</th></tr>
 *   <tr><td>{@code power}</td><td>Identifier</td><td>–</td><td>A single power ID to liberate.</td></tr>
 *   <tr><td>{@code powers}</td><td>Identifier[]</td><td>–</td><td>Additional power IDs to liberate.</td></tr>
 *   <tr><td>{@code power_types}</td><td>Identifier[]</td><td>–</td><td>Factory-type IDs whose suppressed powers are liberated.</td></tr>
 *   <tr><td>{@code power_sources}</td><td>Identifier[]</td><td>–</td><td>Source IDs; suppressed powers granted via a listed source are liberated.</td></tr>
 *   <tr><td>{@code ignored_powers}</td><td>Identifier[]</td><td>–</td><td>Power IDs that are never liberated even if they match the other filters.</td></tr>
 *   <tr><td>{@code bientity_action}</td><td>Bi-entity Action</td><td>–</td><td>Executed once for every power that gets liberated.</td></tr>
 * </table>
 *
 * <h3>Tip – liberate everything</h3>
 * Omitting all filter fields lifts <em>every</em> active suppression on the
 * target in one call.
 */
public class LiberatePowerAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> entities) {
        Entity actor  = entities.getLeft();
        Entity target = entities.getRight();

        // Fast-path: if nothing is suppressed on this entity, skip entirely.
        Set<Identifier> currentlySuppressed =
                SuppressedPowerManager.getSuppressedPowers(target.getUuid());
        if (currentlySuppressed.isEmpty()) return;

        PowerHolderComponent component =
                PowerHolderComponent.KEY.maybeGet(target).orElse(null);
        if (component == null) {
            // Component gone – wipe orphaned suppression state.
            SuppressedPowerManager.removeAll(target.getUuid());
            return;
        }

        // ── Build filter sets ──────────────────────────────────────────────────

        Set<Identifier> powerFilter = new HashSet<>();
        if (data.isPresent("power")) {
            powerFilter.add(data.getId("power"));
        }
        if (data.isPresent("powers")) {
            List<Identifier> list = data.get("powers");
            if (list != null) powerFilter.addAll(list);
        }

        Set<Identifier> typeFilter = new HashSet<>();
        if (data.isPresent("power_types")) {
            List<Identifier> list = data.get("power_types");
            if (list != null) typeFilter.addAll(list);
        }

        Set<Identifier> sourceFilter = new HashSet<>();
        if (data.isPresent("power_sources")) {
            List<Identifier> list = data.get("power_sources");
            if (list != null) sourceFilter.addAll(list);
        }

        Set<Identifier> ignoredPowers = new HashSet<>();
        if (data.isPresent("ignored_powers")) {
            List<Identifier> list = data.get("ignored_powers");
            if (list != null) ignoredPowers.addAll(list);
        }

        boolean hasFilters = !powerFilter.isEmpty()
                || !typeFilter.isEmpty()
                || !sourceFilter.isEmpty();

        @SuppressWarnings("unchecked")
        ActionFactory<Pair<Entity, Entity>>.Instance bientityAction =
                data.isPresent("bientity_action") ? data.get("bientity_action") : null;

        // ── Iterate suppressed powers and lift matching ones ───────────────────

        // We only need to examine powers that are actually suppressed right now.
        // Iterate over a snapshot to avoid ConcurrentModificationException.
        Set<Identifier> snapshot = new HashSet<>(currentlySuppressed);
        int liberatedCount = 0;

        for (Identifier powerId : snapshot) {
            if (ignoredPowers.contains(powerId)) continue;

            boolean shouldLiberate;

            if (!hasFilters) {
                shouldLiberate = true;
            } else {
                shouldLiberate = false;

                // 1. Match by explicit power ID.
                if (!shouldLiberate && powerFilter.contains(powerId)) {
                    shouldLiberate = true;
                }

                // 2. Match by power factory type.
                if (!shouldLiberate && !typeFilter.isEmpty()) {
                    // We need a PowerType to inspect its factory.
                    // PowerTypeRegistry lookup can fail for removed powers – guard it.
                    try {
                        PowerType<?> pt = io.github.apace100.apoli.power.PowerTypeRegistry.get(powerId);
                        Identifier factoryId = pt.getFactory().getFactory().getSerializerId();
                        if (typeFilter.contains(factoryId)) {
                            shouldLiberate = true;
                        }
                    } catch (Exception ignored) {
                        // Power no longer in registry – liberate it anyway to avoid
                        // leaving stale suppression entries.
                        shouldLiberate = true;
                    }
                }

                // 3. Match by grant source (requires the component).
                if (!shouldLiberate && !sourceFilter.isEmpty()) {
                    try {
                        PowerType<?> pt =
                                io.github.apace100.apoli.power.PowerTypeRegistry.get(powerId);
                        for (Identifier src : component.getSources(pt)) {
                            if (sourceFilter.contains(src)) {
                                shouldLiberate = true;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        // Power gone from registry – fall through.
                    }
                }
            }

            if (!shouldLiberate) continue;

            SuppressedPowerManager.liberate(target.getUuid(), powerId);
            liberatedCount++;

            if (bientityAction != null) {
                bientityAction.accept(entities);
            }
        }

        Sync.LOGGER.debug("[Sync/LiberatePower] Liberated {} power(s) on '{}'.",
                liberatedCount, target.getName().getString());
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("liberate_power"),
                new SerializableData()
                        .add("power",          SerializableDataTypes.IDENTIFIER,  null)
                        .add("powers",         SerializableDataTypes.IDENTIFIERS, null)
                        .add("power_types",    SerializableDataTypes.IDENTIFIERS, null)
                        .add("power_sources",  SerializableDataTypes.IDENTIFIERS, null)
                        .add("ignored_powers", SerializableDataTypes.IDENTIFIERS, null)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION,  null),
                LiberatePowerAction::action
        );
    }
}