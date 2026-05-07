package dev.overgrown.sync.registry;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.power.type.custom_projectile.entities.CustomProjectileEntity;
import dev.overgrown.sync.power.type.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.power.type.summons.entities.minion.MinionEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class SyncEntityRegistry {

    public static final EntityType<CustomProjectileEntity> CUSTOM_PROJECTILE = register(
        "custom_projectile",
        EntityType.Builder.<CustomProjectileEntity>create(CustomProjectileEntity::new, SpawnGroup.MISC)
            .dimensions(0.25F, 0.25F)
            .maxTrackingRange(4)
            .trackingTickInterval(10)
    );

    public static final EntityType<MinionEntity> MINION = register(
        "minion",
        EntityType.Builder.<MinionEntity>create(MinionEntity::new, SpawnGroup.CREATURE)
            .dimensions(0.5F, 0.5F)
            .maxTrackingRange(8)
    );

    public static final EntityType<CloneEntity> CLONE = register(
        "clone",
        EntityType.Builder.<CloneEntity>create(CloneEntity::new, SpawnGroup.MISC)
            .dimensions(0.6F, 1.8F)
            .eyeHeight(1.62F)
            .maxTrackingRange(32)
    );

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Sync.identifier(name));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key.getValue().toString()));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(MINION, MinionEntity.createMinionAttributes());
        FabricDefaultAttributeRegistry.register(CLONE, CloneEntity.createCloneAttributes());
    }
}
