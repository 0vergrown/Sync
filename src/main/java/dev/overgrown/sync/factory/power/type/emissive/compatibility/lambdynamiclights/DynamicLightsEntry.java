package dev.overgrown.sync.factory.power.type.emissive.compatibility.lambdynamiclights;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSource;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.power.type.emissive.EmissivePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DynamicLightsEntry implements DynamicLightsInitializer {

    // Custom luminance singleton
    private static final EntityLuminance EMISSIVE_POWER_LUMINANCE = new EntityLuminance() {
        @Override
        public @NotNull Type type() {
            return TYPE;
        }

        @Override
        public int getLuminance(@NotNull dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager itemLightSourceManager, @NotNull Entity entity) {
            if (entity instanceof LivingEntity living) {
                List<EmissivePower> powers = PowerHolderComponent.getPowers(living, EmissivePower.class);
                int maxLight = 0;
                for (EmissivePower power : powers) {
                    if (power.dynamicLight > maxLight) {
                        maxLight = power.dynamicLight;
                    }
                }
                return Math.min(maxLight, 15);
            }
            return 0;
        }
    };

    // Register the type
    public static final EntityLuminance.Type TYPE = EntityLuminance.Type.registerSimple(
            Sync.identifier("emissive_power"),
            EMISSIVE_POWER_LUMINANCE
    );

    @Override
    public void onInitializeDynamicLights(@NotNull DynamicLightsContext context) {
        context.entityLightSourceManager().onRegisterEvent().register(registerContext -> {
            // Create entity light source for players
            EntityLightSource playerLightSource = new EntityLightSource(
                    EntityLightSource.EntityPredicate.builder()
                            .of(EntityType.PLAYER)
                            .build(),
                    List.of(EMISSIVE_POWER_LUMINANCE)
            );

            registerContext.register(playerLightSource);

            // Also register for all living entities to cover clones, minions, etc.
            EntityLightSource allLivingEntitiesLightSource = new EntityLightSource(
                    EntityLightSource.EntityPredicate.builder()
                            .entityType(new dev.lambdaurora.lambdynlights.api.predicate.EntityTypePredicate(
                                    net.minecraft.registry.entry.RegistryEntryList.of(
                                            net.minecraft.registry.Registries.ENTITY_TYPE.streamEntries()
                                                    .filter(entry -> entry.value().getBaseClass().isAssignableFrom(LivingEntity.class))
                                                    .toList()
                                    )
                            ))
                            .build(),
                    List.of(EMISSIVE_POWER_LUMINANCE)
            );

            registerContext.register(allLivingEntitiesLightSource);
        });
    }

    @Override
    @SuppressWarnings("removal")
    public void onInitializeDynamicLights() {}
}