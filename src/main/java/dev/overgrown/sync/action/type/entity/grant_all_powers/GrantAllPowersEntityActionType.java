package dev.overgrown.sync.action.type.entity.grant_all_powers;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GrantAllPowersEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<GrantAllPowersEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("source", SerializableDataTypes.IDENTIFIER),
        data -> new GrantAllPowersEntityActionType(data.get("source")),
        (a, sd) -> sd.instance().set("source", a.source)
    );

    private final Identifier source;

    public GrantAllPowersEntityActionType(Identifier source) {
        this.source = source;
    }

    @Override
    public void accept(EntityActionContext context) {
        Entity entity = context.entity();
        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (component == null) return;

        Set<Identifier> trackedPowerIds = SourcePowerRegistry.getPowersForSource(source);

        if (trackedPowerIds.isEmpty()) {
            Sync.LOGGER.warn("[Sync/GrantAllPowers] No powers tracked for source '{}'. " +
                "Ensure that at least one entity has received powers from this " +
                "source before this action fires.", source);
            return;
        }

        int granted = 0;
        for (Identifier powerId : trackedPowerIds) {
            Power power = PowerManager.getNullable(powerId);
            if (power == null) {
                Sync.LOGGER.warn("[Sync/GrantAllPowers] Skipping '{}' – not in power registry.", powerId);
                continue;
            }

            if (!component.hasPower(power, source)) {
                component.addPower(power, source);
                granted++;
            }
        }

        if (granted > 0) component.sync();

        Sync.LOGGER.debug("[Sync/GrantAllPowers] Granted {}/{} power(s) from '{}' to '{}'.",
            granted, trackedPowerIds.size(), source, entity.getName().getString());
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.GRANT_ALL_POWERS;
    }
}
