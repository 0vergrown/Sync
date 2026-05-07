package dev.overgrown.sync.action.type.bientity.suppress_power;

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

public class SuppressPowerBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<SuppressPowerBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("powers", SerializableDataTypes.IDENTIFIERS.optional(), Optional.empty())
            .add("power_types", SerializableDataTypes.IDENTIFIERS.optional(), Optional.empty())
            .add("power_sources", SerializableDataTypes.IDENTIFIERS.optional(), Optional.empty())
            .add("ignored_powers", SerializableDataTypes.IDENTIFIERS.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new SuppressPowerBiEntityActionType(
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

    public SuppressPowerBiEntityActionType(Optional<Identifier> power,
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

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(target).orElse(null);
        if (component == null) return;

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

        int suppressedCount = 0;
        for (PowerType powerType : component.getPowerTypes()) {
            Identifier powerId = powerType.getPower().getId();
            if (ignored.contains(powerId)) continue;

            boolean shouldSuppress;
            if (!hasFilters) {
                shouldSuppress = true;
            } else {
                shouldSuppress = powerFilter.contains(powerId);
                if (!shouldSuppress && !typeFilter.isEmpty()) {
                    Identifier configId = powerType.getConfig().id();
                    if (typeFilter.contains(configId)) shouldSuppress = true;
                }
                if (!shouldSuppress && !sourceFilter.isEmpty()) {
                    for (Identifier src : component.getSources(powerType.getPower())) {
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

            bientityAction.ifPresent(a -> a.execute(actor, target));
        }

        Sync.LOGGER.debug("[Sync/SuppressPower] Suppressed {} power(s) on '{}'.",
            suppressedCount, target.getName().getString());
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncBiEntityActionTypes.SUPPRESS_POWER;
    }
}
