package dev.overgrown.sync.factory.action.entity;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.util.*;

public class GrantAllPowersAction {
    // Registry to track which powers should be granted from which sources
    private static final Map<Identifier, Set<Identifier>> sourcePowerRegistry = new HashMap<>();

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

            // Check our custom registry for non-Origins sources
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

            // Fallback: Grant all powers from the source's namespace
            // This is less precise but can work for simple cases
            String sourceNamespace = source.getNamespace();
            // Use raw PowerType iterator since PowerTypeRegistry.entries() returns raw types
            for (Map.Entry<Identifier, PowerType> entry : PowerTypeRegistry.entries()) {
                Identifier powerId = entry.getKey();
                if (powerId.getNamespace().equals(sourceNamespace)) {
                    // Cast the raw PowerType to PowerType<?>
                    PowerType<?> powerType = entry.getValue();
                    if (!component.hasPower(powerType, source)) {
                        component.addPower(powerType, source);
                        grantedCount++;
                    }
                }
            }

            component.sync();
            Sync.LOGGER.debug("Granted {} powers from namespace: {}", grantedCount, sourceNamespace);
        }
    }

    // Method to register powers for a specific source
    public static void registerPowersForSource(Identifier source, Identifier... powerIds) {
        Set<Identifier> powerSet = sourcePowerRegistry.computeIfAbsent(source, k -> new HashSet<>());
        powerSet.addAll(Arrays.asList(powerIds));
    }

    // Method to register powers from a data pack structure
    public static void loadPowersFromResourceManager(ResourceManager manager) {
        sourcePowerRegistry.clear();
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
                Sync.identifier("grant_all_powers"),
                new SerializableData()
                        .add("source", SerializableDataTypes.IDENTIFIER),
                GrantAllPowersAction::action
        );
    }
}