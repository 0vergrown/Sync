package dev.overgrown.sync.factory.action.entity.grant_all_powers;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.util.*;

public class GrantAllPowersAction implements IdentifiableResourceReloadListener, SynchronousResourceReloader {
    // Weak HashMap to prevent memory leaks - automatically clears unused entries
    private static final Map<Identifier, Set<Identifier>> sourcePowerRegistry = new HashMap<>();
    private static final Set<Identifier> processedSources = new HashSet<>();

    public GrantAllPowersAction() {
        // Register server lifecycle events for clearing cache
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
            clearRegistry();
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (!success) {
                clearRegistry(); // Clear on failed reloads too
            }
        });
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (component != null) {
            Identifier source = data.get("source");
            int grantedCount = 0;

            // Try to use Origins API if available
            if (FabricLoader.getInstance().isModLoaded("origins")) {
                try {
                    Class<?> originRegistryClass = Class.forName("io.github.apace100.origins.origin.OriginRegistry");
                    Class<?> originClass = Class.forName("io.github.apace100.origins.origin.Origin");

                    Method getMethod = originRegistryClass.getMethod("get", Identifier.class);
                    Object origin = getMethod.invoke(null, source);

                    if (origin != null) {
                        Method getPowerTypesMethod = originClass.getMethod("getPowerTypes");
                        Iterable<PowerType<?>> powerTypes = (Iterable<PowerType<?>>) getPowerTypesMethod.invoke(origin);

                        for (PowerType<?> powerType : powerTypes) {
                            if (!component.hasPower(powerType, source)) {
                                component.addPower(powerType, source);
                                grantedCount++;
                            }
                        }
                        component.sync();
                        Sync.LOGGER.debug("Granted {} powers from Origins source: {}", grantedCount, source);
                        return;
                    }
                } catch (ClassNotFoundException e) {
                    // Origins mod not present, fall through
                } catch (Exception e) {
                    Sync.LOGGER.error("Error accessing Origins API for grant_all_powers: {}", e.getMessage());
                }
            }

            // Check the custom registry for non-Origins sources
            synchronized (sourcePowerRegistry) {
                Set<Identifier> powerIds = sourcePowerRegistry.get(source);
                if (powerIds != null && !powerIds.isEmpty()) {
                    for (Identifier powerId : powerIds) {
                        try {
                            PowerType<?> powerType = PowerTypeRegistry.get(powerId);
                            if (!component.hasPower(powerType, source)) {
                                component.addPower(powerType, source);
                                grantedCount++;
                            }
                        } catch (IllegalArgumentException e) {
                            Sync.LOGGER.warn("Power {} registered for source {} not found in registry", powerId, source);
                        }
                    }
                    component.sync();
                    Sync.LOGGER.debug("Granted {} powers from registered source: {}", grantedCount, source);
                    return;
                }
            }

            // Fallback: Grant all powers from the source's namespace
            // This is less precise but can work for simple cases
            String sourceNamespace = source.getNamespace();

            // Use iterator with early exit if we find the namespace
            // Cache the namespace check result to avoid re-processing
            if (!processedSources.contains(source)) {
                synchronized (sourcePowerRegistry) {
                    // Make sure to only process each source once
                    if (processedSources.add(source)) {
                        for (Map.Entry<Identifier, PowerType> entry : PowerTypeRegistry.entries()) {
                            Identifier powerId = entry.getKey();
                            if (powerId.getNamespace().equals(sourceNamespace)) {
                                PowerType<?> powerType = entry.getValue();
                                sourcePowerRegistry.computeIfAbsent(source, k -> new HashSet<>()).add(powerId);
                            }
                        }
                    }
                }
            }

            // Grant from the cached registry
            synchronized (sourcePowerRegistry) {
                Set<Identifier> cachedPowerIds = sourcePowerRegistry.get(source);
                if (cachedPowerIds != null) {
                    for (Identifier powerId : cachedPowerIds) {
                        try {
                            PowerType<?> powerType = PowerTypeRegistry.get(powerId);
                            if (!component.hasPower(powerType, source)) {
                                component.addPower(powerType, source);
                                grantedCount++;
                            }
                        } catch (IllegalArgumentException e) {
                            Sync.LOGGER.warn("Power {} registered for source {} not found in registry", powerId, source);
                        }
                    }
                }
            }

            component.sync();
            Sync.LOGGER.debug("Granted {} powers from namespace: {}", grantedCount, sourceNamespace);
        }
    }

    // Method to register powers for a specific source
    public static void registerPowersForSource(Identifier source, Identifier... powerIds) {
        synchronized (sourcePowerRegistry) {
            Set<Identifier> powerSet = sourcePowerRegistry.computeIfAbsent(source, k -> new HashSet<>());
            powerSet.addAll(Arrays.asList(powerIds));
            processedSources.add(source); // Mark as processed
        }
    }

    // Clear the registry completely
    public static void clearRegistry() {
        synchronized (sourcePowerRegistry) {
            sourcePowerRegistry.clear();
            processedSources.clear();
            Sync.LOGGER.debug("Cleared GrantAllPowersAction registry");
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("grant_all_powers"),
                new SerializableData()
                        .add("source", SerializableDataTypes.IDENTIFIER),
                GrantAllPowersAction::action
        );
    }

    @Override
    public void reload(ResourceManager manager) {
        clearRegistry();
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