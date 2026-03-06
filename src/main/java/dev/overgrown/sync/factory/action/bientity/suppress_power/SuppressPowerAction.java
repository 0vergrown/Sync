package dev.overgrown.sync.factory.action.bientity.suppress_power;

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
import java.util.stream.Collectors;

public class SuppressPowerAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> entities) {
        Entity actor  = entities.getLeft();
        Entity target = entities.getRight();

        PowerHolderComponent component =
                PowerHolderComponent.KEY.maybeGet(target).orElse(null);
        if (component == null) return;

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

        // ── Diagnostic: warn when a requested power ID is missing from the component ──
        if (!powerFilter.isEmpty()) {
            Set<Identifier> allIds = component.getPowerTypes(true).stream()
                    .map(PowerType::getIdentifier)
                    .collect(Collectors.toSet());
            for (Identifier wanted : powerFilter) {
                if (!allIds.contains(wanted)) {
                    Sync.LOGGER.warn(
                            "[Sync/SuppressPower] Power '{}' is in the filter but NOT present "
                                    + "in '{}'s component. Verify the power ID and that it has been granted. "
                                    + "If it is a sub-power its ID uses '_' not '/' before the sub-key "
                                    + "(e.g. 'namespace:parent_subkey', not 'namespace:parent/subkey'). "
                                    + "Component powers: {}",
                            wanted,
                            target.getName().getString(),
                            allIds
                    );
                }
            }
        }

        @SuppressWarnings("unchecked")
        ActionFactory<Pair<Entity, Entity>>.Instance bientityAction =
                data.isPresent("bientity_action") ? data.get("bientity_action") : null;

        // ── Iterate powers and suppress matches ───────────────────────────────

        int suppressedCount = 0;

        for (PowerType<?> powerType : component.getPowerTypes(true)) {
            Identifier powerId = powerType.getIdentifier();

            if (ignoredPowers.contains(powerId)) continue;

            boolean shouldSuppress;

            if (!hasFilters) {
                shouldSuppress = true;
            } else {
                shouldSuppress = false;

                // 1. Match by explicit power ID.
                if (!shouldSuppress && powerFilter.contains(powerId)) {
                    shouldSuppress = true;
                }

                // 2. Match by power factory type (e.g. "origins:active_self").
                if (!shouldSuppress && !typeFilter.isEmpty()) {
                    try {
                        Identifier factoryId =
                                powerType.getFactory().getFactory().getSerializerId();
                        if (typeFilter.contains(factoryId)) {
                            shouldSuppress = true;
                        }
                    } catch (Exception ignored) {
                        // PowerTypeReference whose target hasn't loaded yet – skip.
                    }
                }

                // 3. Match by grant source.
                if (!shouldSuppress && !sourceFilter.isEmpty()) {
                    for (Identifier src : component.getSources(powerType)) {
                        if (sourceFilter.contains(src)) {
                            shouldSuppress = true;
                            break;
                        }
                    }
                }
            }

            if (!shouldSuppress) continue;

            SuppressedPowerManager.suppress(target.getUuid(), powerId);
            suppressedCount++;

            if (bientityAction != null) {
                bientityAction.accept(entities);
            }
        }

        Sync.LOGGER.debug("[Sync/SuppressPower] Suppressed {} power(s) on '{}'.",
                suppressedCount, target.getName().getString());
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("suppress_power"),
                new SerializableData()
                        .add("power",           SerializableDataTypes.IDENTIFIER,  null)
                        .add("powers",          SerializableDataTypes.IDENTIFIERS, null)
                        .add("power_types",     SerializableDataTypes.IDENTIFIERS, null)
                        .add("power_sources",   SerializableDataTypes.IDENTIFIERS, null)
                        .add("ignored_powers",  SerializableDataTypes.IDENTIFIERS, null)
                        .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION,    null),
                SuppressPowerAction::action
        );
    }
}