package dev.overgrown.sync.factory.action.entity.grant_all_powers;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * <p>Grants every power-type that has previously been granted under the
 * specified {@code source} identifier to the target entity, using the same
 * source.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>{@link dev.overgrown.sync.mixin.grant_all_powers.PowerHolderComponentImplMixin} intercepts every successful
 *       {@code addPower} call in the Apoli component and records the
 *       (source → powerId) pair in {@link SourcePowerRegistry}.</li>
 *   <li>When this action fires it looks up
 *       {@link SourcePowerRegistry#getPowersForSource(Identifier)} to obtain
 *       the set of powers ever granted under that source, then grants any that
 *       the target does not already have.</li>
 * </ol>
 *
 * <p>The registry is automatically cleared on every data-pack reload (via
 * {@link io.github.apace100.apoli.integration.PowerClearCallback}) and
 * repopulated as players re-join or powers are re-granted.
 *
 * <h3>Fields</h3>
 * <ul>
 *   <li>{@code source} – the source identifier to look up, e.g.
 *       {@code origins:my_origin} or {@code apoli:command}</li>
 * </ul>
 *
 * <p>This class also implements {@link IdentifiableResourceReloadListener} so
 * it can be registered as a reload listener and trigger a registry clear on
 * data-pack reloads.
 */
public class GrantAllPowersAction implements IdentifiableResourceReloadListener, SynchronousResourceReloader {

    // ── Action logic ──────────────────────────────────────────────────────────

    public static void action(SerializableData.Instance data, Entity entity) {
        PowerHolderComponent component =
                PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (component == null) return;

        Identifier source = data.get("source");

        Set<Identifier> trackedPowerIds = SourcePowerRegistry.getPowersForSource(source);

        if (trackedPowerIds.isEmpty()) {
            Sync.LOGGER.warn("[Sync/GrantAllPowers] No powers tracked for source '{}'. " + "Ensure that at least one entity has received powers from this "  + "source before this action fires.", source);
            return;
        }

        int granted = 0;
        for (Identifier powerId : trackedPowerIds) {
            PowerType<?> powerType;
            try {
                powerType = PowerTypeRegistry.get(powerId);
            } catch (IllegalArgumentException e) {
                Sync.LOGGER.warn("[Sync/GrantAllPowers] Skipping '{}' – not in power registry: {}",
                        powerId, e.getMessage());
                continue;
            }

            if (!component.hasPower(powerType, source)) {
                component.addPower(powerType, source);
                granted++;
            }
        }

        if (granted > 0) component.sync();

        Sync.LOGGER.debug("[Sync/GrantAllPowers] Granted {}/{} power(s) from '{}' to '{}'.",
                granted, trackedPowerIds.size(), source,
                entity.getName().getString());
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("grant_all_powers"),
                new SerializableData()
                        .add("source", SerializableDataTypes.IDENTIFIER),
                GrantAllPowersAction::action
        );
    }

    // ── IdentifiableResourceReloadListener ────────────────────────────────────

    @Override
    public void reload(ResourceManager manager) {
        // Clearing here is redundant (PowerClearCallback already fires) but
        // serves as an extra safety net in case this listener runs independently.
        SourcePowerRegistry.clear();
    }

    @Override
    public Identifier getFabricId() {
        return Sync.identifier("grant_all_powers_reloader");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.emptyList();
    }
}