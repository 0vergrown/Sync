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

        // Fast-path: nothing suppressed on this entity.
        Set<Identifier> currentlySuppressed = SuppressedPowerManager.getSuppressedPowers(target.getUuid());
        if (currentlySuppressed.isEmpty()) return;

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(target).orElse(null);
        if (component == null) {
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

        // ── Iterate the component's actual power types (mirrors SuppressPowerAction) ──
        //
        // This avoids PowerTypeRegistry.get() lookups and ensures getSources() receives
        // the same PowerType instance the component holds, matching Transfer's approach.

        // Track which suppressed IDs we visited so we can clean up stale ones after.
        Set<Identifier> visited = new HashSet<>();
        int liberatedCount = 0;

        for (PowerType<?> powerType : component.getPowerTypes(true)) {
            Identifier powerId = powerType.getIdentifier();

            // Only consider powers that are currently suppressed.
            if (!currentlySuppressed.contains(powerId)) continue;
            visited.add(powerId);

            if (ignoredPowers.contains(powerId)) continue;

            boolean shouldLiberate;

            if (!hasFilters) {
                shouldLiberate = true;
            } else {
                shouldLiberate = false;

                // 1. Match by explicit power ID.
                if (powerFilter.contains(powerId)) {
                    shouldLiberate = true;
                }

                // 2. Match by power factory type (e.g. "origins:active_self").
                if (!shouldLiberate && !typeFilter.isEmpty()) {
                    try {
                        Identifier factoryId =
                                powerType.getFactory().getFactory().getSerializerId();
                        if (typeFilter.contains(factoryId)) {
                            shouldLiberate = true;
                        }
                    } catch (Exception ignored) {
                        // PowerTypeReference not yet resolved – skip the type filter.
                    }
                }

                // 3. Match by grant source – uses the component-held PowerType directly,
                //    same pattern as TransferAction and SuppressPowerAction.
                if (!shouldLiberate && !sourceFilter.isEmpty()) {
                    for (Identifier src : component.getSources(powerType)) {
                        if (sourceFilter.contains(src)) {
                            shouldLiberate = true;
                            break;
                        }
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

        // ── Clean up stale suppression entries (power removed from component) ──
        //
        // Any suppressed ID not visited above belongs to a power that is no longer
        // in the component. Liberate unconditionally if no filters, or if the ID
        // matches an explicit powerFilter entry (since we can't check type/source
        // for a missing power).
        Set<Identifier> stale = new HashSet<>(currentlySuppressed);
        stale.removeAll(visited);
        for (Identifier staleId : stale) {
            if (ignoredPowers.contains(staleId)) continue;
            if (!hasFilters || powerFilter.contains(staleId)) {
                SuppressedPowerManager.liberate(target.getUuid(), staleId);
                liberatedCount++;
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