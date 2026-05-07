package dev.overgrown.sync.action.type.bientity.liberate_power;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.data.suppress_power.SuppressedPowerManager;
import dev.overgrown.sync.registry.SyncBiEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LiberatePowerBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<LiberatePowerBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("powers", SerializableDataTypes.IDENTIFIERS.optional(), Optional.empty())
            .add("power_types", SerializableDataTypes.IDENTIFIERS.optional(), Optional.empty())
            .add("power_sources", SerializableDataTypes.IDENTIFIERS.optional(), Optional.empty())
            .add("ignored_powers", SerializableDataTypes.IDENTIFIERS.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new LiberatePowerBiEntityActionType(
            data.get("power"),
            data.get("powers"),
            data.get("power_types"),
            data.get("power_sources"),
            data.get("ignored_powers"),
            data.get("bientity_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("power", actionType.power)
            .set("powers", actionType.powers)
            .set("power_types", actionType.powerTypes)
            .set("power_sources", actionType.powerSources)
            .set("ignored_powers", actionType.ignoredPowers)
            .set("bientity_action", actionType.bientityAction)
    );

    private final Optional<Identifier> power;
    private final Optional<List<Identifier>> powers;
    private final Optional<List<Identifier>> powerTypes;
    private final Optional<List<Identifier>> powerSources;
    private final Optional<List<Identifier>> ignoredPowers;
    private final Optional<BiEntityAction> bientityAction;

    public LiberatePowerBiEntityActionType(Optional<Identifier> power,
                                           Optional<List<Identifier>> powers,
                                           Optional<List<Identifier>> powerTypes,
                                           Optional<List<Identifier>> powerSources,
                                           Optional<List<Identifier>> ignoredPowers,
                                           Optional<BiEntityAction> bientityAction) {
        this.power = power;
        this.powers = powers;
        this.powerTypes = powerTypes;
        this.powerSources = powerSources;
        this.ignoredPowers = ignoredPowers;
        this.bientityAction = bientityAction;
    }

    @Override
    public void accept(BiEntityActionContext context) {
        Entity actor = context.actor();
        Entity target = context.target();
        if (target == null) return;

        Set<Identifier> currentlySuppressed = SuppressedPowerManager.getSuppressedPowers(target.getUuid());
        if (currentlySuppressed.isEmpty()) return;

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(target).orElse(null);
        if (component == null) {
            SuppressedPowerManager.removeAll(target.getUuid());
            return;
        }

        Set<Identifier> powerFilter = new HashSet<>();
        power.ifPresent(powerFilter::add);
        powers.ifPresent(powerFilter::addAll);

        Set<Identifier> typeFilter = new HashSet<>();
        powerTypes.ifPresent(typeFilter::addAll);

        Set<Identifier> sourceFilter = new HashSet<>();
        powerSources.ifPresent(sourceFilter::addAll);

        Set<Identifier> ignored = new HashSet<>();
        ignoredPowers.ifPresent(ignored::addAll);

        boolean hasFilters = !powerFilter.isEmpty() || !typeFilter.isEmpty() || !sourceFilter.isEmpty();

        Set<Identifier> visited = new HashSet<>();
        int liberatedCount = 0;

        for (PowerType powerType : component.getPowerTypes()) {
            Identifier powerId = powerType.getPower().getId();
            if (!currentlySuppressed.contains(powerId)) continue;
            visited.add(powerId);
            if (ignored.contains(powerId)) continue;

            boolean shouldLiberate;
            if (!hasFilters) {
                shouldLiberate = true;
            } else {
                shouldLiberate = powerFilter.contains(powerId);
                if (!shouldLiberate && !typeFilter.isEmpty()) {
                    Identifier configId = powerType.getConfig().id();
                    if (typeFilter.contains(configId)) shouldLiberate = true;
                }
                if (!shouldLiberate && !sourceFilter.isEmpty()) {
                    for (Identifier src : component.getSources(powerType.getPower())) {
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
            bientityAction.ifPresent(a -> a.execute(actor, target));
        }

        Set<Identifier> stale = new HashSet<>(currentlySuppressed);
        stale.removeAll(visited);
        for (Identifier staleId : stale) {
            if (ignored.contains(staleId)) continue;
            if (!hasFilters || powerFilter.contains(staleId)) {
                SuppressedPowerManager.liberate(target.getUuid(), staleId);
                liberatedCount++;
            }
        }

        Sync.LOGGER.debug("[Sync/LiberatePower] Liberated {} power(s) on '{}'.",
            liberatedCount, target.getName().getString());
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBiEntityActionTypes.LIBERATE_POWER;
    }
}
